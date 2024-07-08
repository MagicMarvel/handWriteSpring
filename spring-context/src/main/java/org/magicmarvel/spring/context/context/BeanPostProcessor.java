package org.magicmarvel.spring.context.context;

import java.lang.reflect.InvocationTargetException;

public interface BeanPostProcessor {

    /**
     * Invoked after new Bean().
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return bean;
    }

}