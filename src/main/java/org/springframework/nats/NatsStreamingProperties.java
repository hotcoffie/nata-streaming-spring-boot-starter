package org.springframework.nats;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:
 *
 * @author 谢宇
 * Date: 2019/7/9
 */
@ConfigurationProperties(prefix = "spring.nats.streaming")
@Getter
@Setter
public class NatsStreamingProperties {
    private String urls = "nats://localhost:4222";
    private String clusterId = "test-cluster";
    private String clientId = "test-client";

}
