package com.magicmarvel.scan.proxy;


import org.magicmarvel.spring.context.annotation.Autowired;
import org.magicmarvel.spring.context.annotation.Component;

@Component
public record InjectProxyOnConstructorBean(OriginBean injected) {

    public InjectProxyOnConstructorBean(@Autowired OriginBean injected) {
        this.injected = injected;
    }
}
