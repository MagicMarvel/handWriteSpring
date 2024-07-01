package com.magicmarvel.scan.nested;


import org.magicmarvel.spring.context.annotation.Component;

@Component
public class OuterBean {

    @Component
    public static class NestedBean {

    }
}
