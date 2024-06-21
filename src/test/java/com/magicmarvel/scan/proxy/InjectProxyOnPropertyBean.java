package com.magicmarvel.scan.proxy;

import com.magicmarvel.handWriteSpring.annotation.Autowired;
import com.magicmarvel.handWriteSpring.annotation.Component;

@Component
public class InjectProxyOnPropertyBean {

    @Autowired
    public OriginBean injected;
}
