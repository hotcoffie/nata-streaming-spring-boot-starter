package org.springframework.nats;

import io.nats.client.Connection;
import io.nats.streaming.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.nats.properties.NatsStreamingProperties;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Description:
 *
 * @author 谢宇
 * Date: 2019/8/30 030 下午 9:11
 */
@Slf4j
public class NatsStreamingTemplate implements StreamingConnection {
    private StreamingConnection sc;

    void connect(NatsStreamingProperties properties) {
        Options opts = new Options.Builder()
                .natsUrl(properties.getUrls())
                .maxPingsOut(3)
                .pingInterval(Duration.ofSeconds(3))
                .connectionLostHandler((conn, ex) -> {
                    this.connect(properties);
                })
                .build();
        while (true) {
            try {
                this.sc = NatsStreaming.connect(properties.getClusterId(), properties.getClientId(), opts);
                break;
            } catch (IOException | InterruptedException e) {
                log.error("连接丢失，稍后将重新连接", e);
            }

            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                log.error("重连延时失败！", e);
            }
        }
        log.info("连接成功：" + properties.getUrls());
    }

    @Override
    public void publish(String subject, byte[] data) throws IOException, InterruptedException, TimeoutException {
        sc.publish(subject, data);
    }

    @Override
    public String publish(String subject, byte[] data, AckHandler ah) throws IOException, InterruptedException, TimeoutException {
        return sc.publish(subject, data, ah);
    }

    @Override
    public Subscription subscribe(String subject, MessageHandler cb) throws IOException, InterruptedException, TimeoutException {
        return sc.subscribe(subject, cb);
    }

    @Override
    public Subscription subscribe(String subject, MessageHandler cb, SubscriptionOptions opts) throws IOException, InterruptedException, TimeoutException {
        return sc.subscribe(subject, cb, opts);
    }

    @Override
    public Subscription subscribe(String subject, String queue, MessageHandler cb) throws IOException, InterruptedException, TimeoutException {
        return sc.subscribe(subject, queue, cb);
    }

    @Override
    public Subscription subscribe(String subject, String queue, MessageHandler cb, SubscriptionOptions opts) throws IOException, InterruptedException, TimeoutException {
        return sc.subscribe(subject, queue, cb, opts);
    }

    @Override
    public Connection getNatsConnection() {
        return sc.getNatsConnection();
    }

    @Override
    public void close() throws IOException, TimeoutException, InterruptedException {
        sc.close();
    }
}
