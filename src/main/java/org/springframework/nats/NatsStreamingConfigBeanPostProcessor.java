package org.springframework.nats;

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

    private StreamingConnection sc;
    private SubscriptionOptions options;
    @Autowired
    private Environment env;

    public NatsStreamingConfigBeanPostProcessor(StreamingConnection sc, SubscriptionOptions options) {
        this.sc = sc;
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
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1 || !parameterTypes[0].equals(Message.class)) {
                    throw new NatsStreamingException(String.format(
                            "Method '%s' on bean with name '%s' must have a single parameter of type %s when using the @%s annotation.",
                            method.toGenericString(),
                            beanName,
                            Message.class.getName(),
                            NatsStreamingSubscribe.class.getName()
                    ));
                }
                String topic = subscribe.subscribe();
                //如果用户配置了外部配置的主题，则覆盖代码内主题
                String propertyPath = subscribe.propertyPath();
                if (StringUtils.hasLength(propertyPath)) {
                    String topicTemp = env.getProperty(propertyPath);
                    topic = topicTemp != null ? topicTemp : topic;
                }

                try {
                    sc.subscribe(topic, "".equals(subscribe.queue()) ? null : subscribe.queue(), msg -> {
                        try {
                            method.invoke(bean, msg);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            log.error("订阅回调失败", e);
                        }
                    }, options);
                } catch (IOException | InterruptedException | TimeoutException e) {
                    log.error("nats异常", e);
                    Thread.currentThread().interrupt();
                }
            });
        });
        return bean;
    }
}
