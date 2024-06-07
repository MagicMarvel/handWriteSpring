package com.magicmarvel.scan.custom.annotation;


import com.magicmarvel.handWriteSpring.annotation.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CustomAnnotation {

    String value() default "";

}
