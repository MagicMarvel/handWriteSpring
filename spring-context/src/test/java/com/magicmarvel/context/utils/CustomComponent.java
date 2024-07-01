package com.magicmarvel.context.utils;


import org.magicmarvel.spring.context.annotation.AliasFor;
import org.magicmarvel.spring.context.annotation.Component;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CustomComponent {

    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";

}
