package org.magicmarvel.spring.context.exception;

public class NoSuchBeanDefinitionException extends BeanDefinitionException {

    public NoSuchBeanDefinitionException() {
    }

    public NoSuchBeanDefinitionException(String message) {
        super(message);
    }
}
