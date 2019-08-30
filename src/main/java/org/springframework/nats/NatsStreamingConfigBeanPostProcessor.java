package org.springframework.nats;

import io.nats.client.Dispatcher;
import io.nats.streaming.Message;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.SubscriptionOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.nats.annotation.NatsStreamingSubscribe;
import org.springframework.nats.enums.ConnectionType;
import org.springframework.nats.exception.NatsStreamingException;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Nats Streaming 处理类 主要是注册{@link NatsStreamingSubscribe}
 *
 * @author 谢宇
 * Date: 2019/7/9
 */
@Slf4j
public class NatsStreamingConfigBeanPostProcessor implements BeanPostProcessor {

    private StreamingConnection template;
    private SubscriptionOptions options;
    @Autowired
    private Environment env;

    public NatsStreamingConfigBeanPostProcessor(NatsStreamingTemplate template, SubscriptionOptions options) {
        this.template = template;
        this.options = options;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        final Class<?> clazz = bean.getClass();
        Arrays.stream(clazz.getMethods()).forEach(method -> {
            Optional<NatsStreamingSubscribe> sub = Optional.ofNullable(AnnotationUtils.findAnnotation(method, NatsStreamingSubscribe.class));
            sub.ifPresent(subscribe -> {

                String topic = subscribe.subscribe();
                //如果用户配置了外部配置的主题，则覆盖代码内主题
                String propertyPath = subscribe.propertyPath();
                if (StringUtils.hasLength(propertyPath)) {
                    String topicTemp = env.getProperty(propertyPath);
                    topic = topicTemp != null ? topicTemp : topic;
                }

                String queue = subscribe.queue();

                final Class<?>[] parameterTypes = method.getParameterTypes();
                NatsStreamingException en = new NatsStreamingException(String.format(
                        "Method '%s' on bean with name '%s' must have a single parameter of type %s when using the @%s annotation.",
                        method.toGenericString(),
                        beanName,
                        Message.class.getName(),
                        NatsStreamingSubscribe.class.getName()
                ));

                if (subscribe.connectionType() == ConnectionType.Nats) {
                    if (parameterTypes.length != 1 || !parameterTypes[0].equals(io.nats.client.Message.class)) {
                        throw en;
                    }
                    Dispatcher d = template.getNatsConnection().createDispatcher(msg -> {
                        try {
                            method.invoke(bean, msg);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            log.error("订阅回调失败", e);
                        }
                    });

                    //这愚蠢的写法完全是因为写驱动的人非要在subscribe(topic, queue)中要求queue不能为空
                    if ("".equals(queue)) {
                        d.subscribe(topic);
                    } else {
                        d.subscribe(topic, queue);
                    }
                    log.info("成功订阅Nats消息，主题：{}", topic);
                } else {
                    if (parameterTypes.length != 1 || !parameterTypes[0].equals(Message.class)) {
                        throw en;
                    }

                    queue = "".equals(queue) ? null : queue;
                    try {
                        template.subscribe(topic, queue, msg -> {
                            try {
                                method.invoke(bean, msg);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                log.error("订阅回调失败", e);
                            }
                        }, options);
                        log.info("成功订阅Nats Streaming消息，主题：{}", topic);
                    } catch (IOException | InterruptedException | TimeoutException e) {
                        log.error("Nats Streaming异常", e);
                        Thread.currentThread().interrupt();
                    }
                }

            });
        });
        return bean;
    }
}
