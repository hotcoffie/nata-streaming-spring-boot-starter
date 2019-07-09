package org.springframework.nats.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.nats.NatsStreamingConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Nats Support {@link }
 *
 * @author wanli
 * @date 2018-09-17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({NatsStreamingConfiguration.class})
public @interface EnableNatsStreaming {
}
