package org.magicmarvel.spring.context.context;

import jakarta.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface ConfigurableApplicationContext extends ApplicationContext {

    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(String name);

    @Nullable
    BeanDefinition findBeanDefinition(String name, Class<?> requiredType);

    Object createBeanAsEarlySingleton(BeanDefinition def) throws InvocationTargetException, InstantiationException, IllegalAccessException;
}
