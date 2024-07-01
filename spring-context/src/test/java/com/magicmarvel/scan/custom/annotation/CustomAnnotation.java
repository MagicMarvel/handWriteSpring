package com.magicmarvel.scan.custom.annotation;


import org.magicmarvel.handWriteSpring.annotation.AliasFor;
import org.magicmarvel.handWriteSpring.annotation.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface CustomAnnotation {
    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";
}
