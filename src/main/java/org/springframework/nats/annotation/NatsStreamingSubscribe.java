package org.springframework.nats.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.nats.enums.ConnectionType;

import java.lang.annotation.*;

/**
 * @author 谢宇
 * Date: 2019/7/9
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface NatsStreamingSubscribe {

    /**
     * @return {@link #subscribe()}
     */
    @AliasFor("subscribe")
    String value() default "";

    /**
     * The Nats subject to subscribe to.
     *
     * @return the Nats Subject
     */
    @AliasFor("value")
    String subscribe() default "";

    /**
     * Queue name
     *
     * @return the nats queue name
     */
    String queue() default "";

    /**
     * 如果用户配置了这个属性，则从配置文件指定位置获取消息主题，取代subscribe属性配置的主题
     *
     * @return 配置在yml文件中的位置
     */
    String propertyPath() default "";

    /**
     * 订阅方式，默认使用NatsStreaming订阅
     *
     * @return 订阅方式枚举
     */
    ConnectionType connectionType() default ConnectionType.NatsStreaming;
}
