package org.springframework.nats;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.streaming.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.nats.annotation.NatsStreamingSubscribe;
import org.springframework.nats.enums.ConnectionType;
import org.springframework.nats.exception.NatsStreamingException;
import org.springframework.nats.properties.NatsStreamingProperties;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Description:
 *
 * @author 谢宇
 * Date: 2019/8/30 030 下午 9:11
 */
@Slf4j
public class NatsStreamingTemplate {
    @Resource
    private Environment env;
    // 连接实例
    private StreamingConnection sc;
    private SubscriptionOptions options;
    private HashMap<String, Object> subBeans = new HashMap<>();

    NatsStreamingTemplate(SubscriptionOptions options) {
        this.options = options;
    }

    /**
     * 连接nats streaming
     *
     * @param properties yaml配置
     */
    void connect(NatsStreamingProperties properties) {
        Options opts = new Options.Builder()
                .natsUrl(properties.getUrls())
                .maxPingsOut(3)
                .pingInterval(Duration.ofSeconds(3))
                .connectionLostHandler((conn, ex) -> {
                    this.connect(properties);
                    subBeans.forEach((beanName, bean) -> doSub(bean, beanName, true));
                })
                .build();
        while (true) {
            log.info("开始连接 Nats Streaming({})...", properties.getUrls());
            try {
                this.sc = NatsStreaming.connect(properties.getClusterId(), properties.getClientId(), opts);
                break;
            } catch (IOException | InterruptedException e) {
                log.error("Nats Streaming(" + properties.getUrls() + ")连接失败，" + properties.getInterval() + "秒后重新尝试连接", e);
            }

            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(properties.getInterval()));
            } catch (InterruptedException e) {
                log.error("重连延时失败！", e);
            }
        }
        log.info("Nats Streaming({}) 连接成功！", properties.getUrls());
    }

    void doSub(Object bean, String beanName) {
        doSub(bean, beanName, false);
    }

    /**
     * 为提供注解的bean进行消息订阅
     */
    private void doSub(Object bean, String beanName, boolean dataFormSubBeans) {
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
                    Dispatcher d = getNatsConnection().createDispatcher(msg -> {
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
                        subscribe(topic, queue, msg -> {
                            try {
                                log.info("收到消息：{}", new String(msg.getData(), StandardCharsets.UTF_8));
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
                if (!dataFormSubBeans) {
                    this.subBeans.put(beanName, bean);
                }
            });
        });
    }

    public void publish(String subject, byte[] data) throws IOException, InterruptedException, TimeoutException {
        sc.publish(subject, data);
    }

    public String publish(String subject, byte[] data, AckHandler ah) throws IOException, InterruptedException, TimeoutException {
        return sc.publish(subject, data, ah);
    }

    public Subscription subscribe(String subject, MessageHandler cb) throws IOException, InterruptedException, TimeoutException {
        return sc.subscribe(subject, cb);
    }

    public Subscription subscribe(String subject, MessageHandler cb, SubscriptionOptions opts) throws IOException, InterruptedException, TimeoutException {
        return sc.subscribe(subject, cb, opts);
    }

    public Subscription subscribe(String subject, String queue, MessageHandler cb) throws IOException, InterruptedException, TimeoutException {
        return sc.subscribe(subject, queue, cb);
    }

    public Subscription subscribe(String subject, String queue, MessageHandler cb, SubscriptionOptions opts) throws IOException, InterruptedException, TimeoutException {
        return sc.subscribe(subject, queue, cb, opts);
    }

    public Connection getNatsConnection() {
        return sc.getNatsConnection();
    }

    public void close() throws IOException, TimeoutException, InterruptedException {
        sc.close();
    }
}
