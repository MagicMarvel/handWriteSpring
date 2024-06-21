package com.magicmarvel.scan.proxy;

import com.magicmarvel.handWriteSpring.annotation.Component;
import com.magicmarvel.handWriteSpring.annotation.Order;
import com.magicmarvel.handWriteSpring.context.BeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Order(200)
@Component
public class SecondProxyBeanPostProcessor implements BeanPostProcessor {

    final Logger logger = LoggerFactory.getLogger(getClass());

    final Map<String, Object> originBeans = new HashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (OriginBean.class.isAssignableFrom(bean.getClass())) {
            logger.debug("create second proxy for bean '{}': {}", beanName, bean);
            var proxy = new SecondProxyBean((OriginBean) bean);
            originBeans.put(beanName, bean);
            return proxy;
        }
        return bean;
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = originBeans.get(beanName);
        if (origin != null) {
            logger.debug("auto set property for {} from second proxy {} to origin bean: {}", beanName, bean, origin);
            return origin;
        }
        return bean;
    }
}
