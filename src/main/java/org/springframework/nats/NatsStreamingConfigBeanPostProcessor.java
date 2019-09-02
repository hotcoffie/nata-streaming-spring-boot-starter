package org.springframework.nats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.nats.annotation.NatsStreamingSubscribe;

/**
 * Nats Streaming 处理类 主要是注册{@link NatsStreamingSubscribe}
 *
 * @author 谢宇
 * Date: 2019/7/9
 */
@Slf4j
public class NatsStreamingConfigBeanPostProcessor implements BeanPostProcessor {
    private NatsStreamingTemplate template;

    NatsStreamingConfigBeanPostProcessor(NatsStreamingTemplate template) {
        this.template = template;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        template.doSub(bean, beanName);
        return bean;
    }
}
