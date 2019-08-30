package org.springframework.nats.enums;

/**
 * Description:
 *
 * @author 谢宇
 * Date: 2019/8/30 030 下午 9:06
 */

public enum ConnectionType {
    /**
     * 使用nats连接
     */
    Nats,
    /**
     * 使用NatsStreaming连接
     */
    NatsStreaming;
}
