package com.magicmarvel.scan.proxy;

import org.magicmarvel.spring.context.annotation.Component;
import org.magicmarvel.spring.context.annotation.Order;
import org.magicmarvel.spring.context.context.BeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Order(100)
@Component
public class FirstProxyBeanPostProcessor implements BeanPostProcessor {

    final Logger logger = LoggerFactory.getLogger(getClass());

    final Map<String, Object> originBeans = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (OriginBean.class.isAssignableFrom(bean.getClass())) {
            logger.debug("create first proxy for bean '{}': {}", beanName, bean);
            var proxy = new FirstProxyBean((OriginBean) bean);
            originBeans.put(beanName, bean);
            return proxy;
        }
        return bean;
    }
}
