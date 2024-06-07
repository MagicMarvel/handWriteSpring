package com.magicmarvel.handWriteSpring.utils;

import com.magicmarvel.handWriteSpring.annotation.Component;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CustomComponent {

    String value() default "";

}
