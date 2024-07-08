package com.magicmarvel.spring.aop.before;


import org.magicmarvel.spring.aop.invocationHandler.BeforeInvocationHandlerAdapter;
import org.magicmarvel.spring.context.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@Component
public class LogInvocationHandler extends BeforeInvocationHandlerAdapter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void before(Object proxy, Method method, Object[] args) {
        logger.info("[Before] {}()", method.getName());
    }
}
