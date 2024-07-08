package org.magicmarvel.spring.context.context;

import jakarta.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 用于相比于客户端专用API的ApplicationContext，这里还有一些框架需要用的API
 */
public interface ConfigurableApplicationContext {

    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(String name);

    @Nullable
    BeanDefinition findBeanDefinition(String name, Class<?> requiredType);

    Object createBeanAsEarlySingleton(BeanDefinition def) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException;
}
