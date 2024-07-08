package org.magicmarvel.spring.context.context;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

public class ApplicationContextUtils {

    private static AnnotationConfigApplicationContext applicationContext = null;

    @Nonnull
    public static ApplicationContext getRequiredApplicationContext() {
        return Objects.requireNonNull(getApplicationContext(), "ApplicationContext is not set.");
    }

    @Nonnull
    public static ConfigurableApplicationContext getRequiredConfigurableApplicationContext() {
        return Objects.requireNonNull(getConfigurableApplicationContext(), "ConfigurableApplicationContext is not set.");
    }

    @Nullable
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Nullable
    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return applicationContext;
    }

    static void setApplicationContext(AnnotationConfigApplicationContext ctx) {
        applicationContext = ctx;
    }

}
