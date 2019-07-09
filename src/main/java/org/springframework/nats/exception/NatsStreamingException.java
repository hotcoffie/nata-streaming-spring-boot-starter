package org.springframework.nats.exception;

/**
 * The Spring NatsStreamingException implementation
 *
 * @author 谢宇
 * Date: 2019/7/9
 */
public class NatsStreamingException extends RuntimeException {

    public NatsStreamingException(Exception e) {
        super(e);
    }

    public NatsStreamingException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
