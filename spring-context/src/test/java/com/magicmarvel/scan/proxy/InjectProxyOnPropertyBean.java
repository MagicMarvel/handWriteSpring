package com.magicmarvel.scan.proxy;


import org.magicmarvel.handWriteSpring.annotation.Autowired;
import org.magicmarvel.handWriteSpring.annotation.Component;

@Component
public class InjectProxyOnPropertyBean {

    @Autowired
    public OriginBean injected;
}
