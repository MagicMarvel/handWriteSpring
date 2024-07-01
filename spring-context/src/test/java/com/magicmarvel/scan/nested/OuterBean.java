package com.magicmarvel.scan.nested;


import org.magicmarvel.handWriteSpring.annotation.Component;

@Component
public class OuterBean {

    @Component
    public static class NestedBean {

    }
}
