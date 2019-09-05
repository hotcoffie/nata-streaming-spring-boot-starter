package org.springframework.nats;

import io.nats.streaming.AckHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 用于缓存发送失败的消息
 *
 * @author 小谢
 * Date: 2019/09/05 下午 4:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublishCache {
    private String subject;
    private byte[] data;
    private AckHandler ah;

    public PublishCache(String subject, byte[] data) {
        this.subject = subject;
        this.data = data;
    }
}
