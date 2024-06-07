package com.magicmarvel.scan.nested;


import com.magicmarvel.handWriteSpring.annotation.Component;

@Component
public class OuterBean {

    @Component
    public static class NestedBean {

    }
}
