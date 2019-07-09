package org.springframework.nats;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;

/**
 * @author 谢宇
 * Date: 2019/7/9
 */
@ConfigurationProperties(prefix = "spring.nats.streaming.subscribe")
@Getter
@Setter
public class NatsStreamingSubProperties {
    private String durableName;
    private Integer maxInFlight = 1024;
    private Long ackWait = 30L;
    private Long startSequence;
    private Instant startTime;
    private boolean manualAcks = false;
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    private String startTimeAsDate;
    private Integer subscriptionTimeout = 2;
    private boolean deliverAllAvailable = true;
    private boolean startWithLastReceived = false;
}
