package com.magicmarvel.spring.aop.after;

import org.magicmarvel.spring.aop.invocationHandler.AfterInvocationHandlerAdapter;
import org.magicmarvel.spring.context.annotation.Component;

import java.lang.reflect.Method;

@Component
public class PoliteInvocationHandler extends AfterInvocationHandlerAdapter {

    @Override
    public Object after(Object proxy, Object returnValue, Method method, Object[] args) {
        if (returnValue instanceof String s) {
            if (s.endsWith(".")) {
                return s.substring(0, s.length() - 1) + "!";
            }
        }
        return returnValue;
    }
}
