package com.magicmarvel.scan.proxy;


import org.magicmarvel.spring.context.annotation.Autowired;
import org.magicmarvel.spring.context.annotation.Component;

@Component
public class InjectProxyOnPropertyBean {

    @Autowired
    public OriginBean injected;
}
