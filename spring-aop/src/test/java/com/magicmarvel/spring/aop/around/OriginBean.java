package com.magicmarvel.spring.aop.around;

import org.magicmarvel.spring.aop.annotation.Around;
import org.magicmarvel.spring.context.annotation.Component;
import org.magicmarvel.spring.context.annotation.Value;

@Component
@Around("aroundInvocationHandler")
public class OriginBean {

    @Value("${customer.name}")
    public String name;

    @Polite
    public String hello() {
        return "Hello, " + name + ".";
    }

    public String morning() {
        return "Morning, " + name + ".";
    }
}
