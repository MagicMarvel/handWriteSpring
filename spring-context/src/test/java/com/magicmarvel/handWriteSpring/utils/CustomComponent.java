package com.magicmarvel.handWriteSpring.utils;


import org.magicmarvel.handWriteSpring.annotation.AliasFor;
import org.magicmarvel.handWriteSpring.annotation.Component;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CustomComponent {

    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";

}
