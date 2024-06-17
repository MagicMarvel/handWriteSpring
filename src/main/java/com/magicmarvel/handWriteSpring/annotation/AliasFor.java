package com.magicmarvel.handWriteSpring.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AliasFor {
    String value() default "";

    Class<?> annotation();

    String attribute() default "";
}
