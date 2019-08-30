package org.springframework.nats;

import io.nats.streaming.SubscriptionOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.nats.properties.NatsStreamingProperties;
import org.springframework.nats.properties.NatsStreamingSubProperties;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author 谢宇
 * Date: 2019/7/9
 */
@Configuration
@EnableConfigurationProperties({NatsStreamingProperties.class, NatsStreamingSubProperties.class})
@Slf4j
public class NatsStreamingConfiguration {
    @Bean
    public NatsStreamingTemplate streamingConnection(NatsStreamingProperties properties) {
        NatsStreamingTemplate template = new NatsStreamingTemplate();
        template.connect(properties);
        return template;

    }


    @Bean
    public SubscriptionOptions options(NatsStreamingSubProperties properties) {
        SubscriptionOptions.Builder builder = new SubscriptionOptions.Builder();
        builder.maxInFlight(properties.getMaxInFlight())
                .ackWait(properties.getAckWait(), TimeUnit.SECONDS)
                .deliverAllAvailable();
        if (properties.isManualAcks()) {
            builder.manualAcks();
        }
        String durableName = properties.getDurableName();
        if (StringUtils.hasLength(durableName)) {
            builder.durableName(durableName);
        }
        return builder.build();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public NatsStreamingConfigBeanPostProcessor configBeanPostProcessor(NatsStreamingTemplate template, SubscriptionOptions options) {
        return new NatsStreamingConfigBeanPostProcessor(template, options);
    }
}
