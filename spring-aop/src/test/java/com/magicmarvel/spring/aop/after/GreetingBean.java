package com.magicmarvel.spring.aop.after;

import org.magicmarvel.spring.aop.annotation.Around;
import org.magicmarvel.spring.context.annotation.Component;

@Component
@Around("politeInvocationHandler")
public class GreetingBean {

    public String hello(String name) {
        return "Hello, " + name + ".";
    }

    public String morning(String name) {
        return "Morning, " + name + ".";
    }
}
