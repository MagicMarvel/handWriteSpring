package com.magicmarvel.spring.aop.around;


import org.magicmarvel.spring.context.annotation.Autowired;
import org.magicmarvel.spring.context.annotation.Component;
import org.magicmarvel.spring.context.annotation.Order;

@Order(0)
@Component
public class OtherBean {

    public final OriginBean origin;

    public OtherBean(@Autowired OriginBean origin) {
        this.origin = origin;
    }
}
