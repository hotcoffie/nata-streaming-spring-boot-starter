package org.springframework.nats.annotation;

import org.springframework.core.annotation.AliasFor;

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
}
