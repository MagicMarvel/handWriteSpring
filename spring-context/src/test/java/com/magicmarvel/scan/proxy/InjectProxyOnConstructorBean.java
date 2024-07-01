package com.magicmarvel.scan.proxy;


import org.magicmarvel.handWriteSpring.annotation.Autowired;
import org.magicmarvel.handWriteSpring.annotation.Component;

@Component
public record InjectProxyOnConstructorBean(OriginBean injected) {

    public InjectProxyOnConstructorBean(@Autowired OriginBean injected) {
        this.injected = injected;
    }
}
