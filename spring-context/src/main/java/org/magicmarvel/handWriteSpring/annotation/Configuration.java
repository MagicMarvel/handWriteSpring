package org.magicmarvel.handWriteSpring.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {

    /**
     * Bean name. Default to simple class name with first-letter-lower-case.
     */
    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";
}
