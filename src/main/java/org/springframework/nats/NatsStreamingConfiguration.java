package org.springframework.nats;

import io.nats.streaming.NatsStreaming;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.SubscriptionOptions;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * @author 谢宇
 * Date: 2019/7/9
 */
@Configuration
@EnableConfigurationProperties({NatsStreamingProperties.class, NatsStreamingSubProperties.class})
public class NatsStreamingConfiguration {
    @Bean
    public StreamingConnection streamingConnection(NatsStreamingProperties properties) throws IOException, InterruptedException {
        Options opts = new Options.Builder().natsUrl(properties.getUrls()).build();
        return NatsStreaming.connect(properties.getClusterId(), properties.getClientId(), opts);
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
        return builder.build();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public NatsStreamingConfigBeanPostProcessor configBeanPostProcessor(StreamingConnection connection, SubscriptionOptions options) {
        return new NatsStreamingConfigBeanPostProcessor(connection, options);
    }
}
