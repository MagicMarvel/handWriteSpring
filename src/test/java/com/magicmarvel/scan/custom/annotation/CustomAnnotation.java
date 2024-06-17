package com.magicmarvel.scan.custom.annotation;


import com.magicmarvel.handWriteSpring.annotation.AliasFor;
import com.magicmarvel.handWriteSpring.annotation.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CustomAnnotation {
    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";
}
