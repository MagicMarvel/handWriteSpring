package com.magicmarvel.scan.proxy;

import com.magicmarvel.handWriteSpring.annotation.Autowired;
import com.magicmarvel.handWriteSpring.annotation.Component;

@Component
public record InjectProxyOnConstructorBean(OriginBean injected) {

    public InjectProxyOnConstructorBean(@Autowired OriginBean injected) {
        this.injected = injected;
    }
}
