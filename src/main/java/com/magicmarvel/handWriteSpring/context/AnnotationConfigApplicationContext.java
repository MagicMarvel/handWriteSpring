package com.magicmarvel.handWriteSpring.context;

import com.magicmarvel.handWriteSpring.annotation.*;
import com.magicmarvel.handWriteSpring.exception.BeanDefinitionException;
import com.magicmarvel.handWriteSpring.io.property.PropertyResolver;
import com.magicmarvel.handWriteSpring.io.resource.ResourceResolver;
import com.magicmarvel.handWriteSpring.utils.ClassUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;


public class AnnotationConfigApplicationContext {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PropertyResolver propertyResolver;
    private Map<String, BeanDefinition> beans = new HashMap<>();

    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) throws URISyntaxException, IOException {
        this.propertyResolver = propertyResolver;

        // 扫描获取所有Bean的Class类型:
        final Set<String> beanClassNames = scanForClassNames(configClass);

        // 创建Bean的定义:
        this.beans = createBeanDefinitions(beanClassNames);

    }

    /**
     * 针对给定的起点class，扫描他的ComponentScan注解，扫描注解里指定的包路径，获取所有Bean的包路径，待会用getName方法获取Class
     *
     * @param configClass 起点class
     * @return Bean的包路径集合
     */
    private Set<String> scanForClassNames(Class<?> configClass) throws URISyntaxException, IOException {
        // 要在哪个包里扫描
        String[] scanPackages;

        // 获取ComponentScan注解，如果没有则默认扫描当前包路径
        ComponentScan componentScanAnno = ClassUtils.findAnnotation(configClass, ComponentScan.class);
        if (componentScanAnno == null || componentScanAnno.value().length == 0) {
            scanPackages = new String[]{configClass.getPackageName()};
        } else {
            scanPackages = componentScanAnno.value();
        }
        logger.atDebug().log("Scan packages in: {}", Arrays.toString(scanPackages));

        // 所有的要被框架管理的bean的包路径
        Set<String> classNameSet = new HashSet<>();

        for (String scanPackage : scanPackages) {
            ResourceResolver resourceResolver = new ResourceResolver(scanPackage);
            List<String> scanClasses = resourceResolver.scan(resource -> {
                if (resource.name().endsWith(".class")) {
                    String className = resource.name()
                            .replace("/", ".")
                            .replace("\\", ".")
                            .substring(0, resource.name().length() - 6);
                    logger.atDebug().log("Found class: {}", className);
                    return className;
                }
                return null;
            });
            classNameSet.addAll(scanClasses);
        }

        // 处理Import注解，Import注解里加的类对象都要被框架管理
        Import importAnno = ClassUtils.findAnnotation(configClass, Import.class);
        if (importAnno != null) {
            for (Class<?> importClass : importAnno.value()) {
                classNameSet.add(importClass.getName());
            }
        }
        return classNameSet;
    }

    private Map<String, BeanDefinition> createBeanDefinitions(Set<String> beanClassNames) {
        Map<String, BeanDefinition> beans = new HashMap<>();
        for (String beanClassName : beanClassNames) {
            try {
                Class<?> beanClass = Class.forName(beanClassName);
                // 找到所有标记了Component的类，这些类都是Bean，Configuration也是Component
                if (ClassUtils.findAnnotation(beanClass, Component.class) != null &&
                        !beanClass.isAnnotation() &&
                        !beanClass.isInterface() &&
                        !beanClass.isEnum() &&
                        !beanClass.isPrimitive()
                ) {
                    if (Objects.equals(beanClassName, "com.magicmarvel.imported.ZonedDateConfiguration")) {
                        System.out.println("aaa");
                    }
                    String beanName = ClassUtils.getBeanName(beanClass);
                    BeanDefinition beanDefinition = new BeanDefinition(beanName,
                            beanClass,
                            getConstructor(beanClass),
                            getOrder(beanClass),
                            isPrimary(beanClass),
                            null,
                            null,
                            ClassUtils.findAnnotationMethod(beanClass, PostConstruct.class),
                            ClassUtils.findAnnotationMethod(beanClass, PreDestroy.class));
                    logger.atDebug().log("Create bean definition by @Component: {}", beanDefinition);
                    beans.put(beanName, beanDefinition);


                    // 如果是Configuration，还要找到所有的标记为Bean的方法
                    if (beanClass.isAnnotationPresent(Configuration.class)) {
                        for (Method method : beanClass.getDeclaredMethods()) {
                            if (method.isAnnotationPresent(Bean.class)) {
                                String methodName = ClassUtils.getBeanName(method);
                                System.out.println(methodName);
                                Bean bean = method.getAnnotation(Bean.class);
                                BeanDefinition methodBeanDefinition = new BeanDefinition(
                                        methodName,
                                        method.getReturnType(),
                                        beanName,
                                        method,
                                        getOrder(method),
                                        method.isAnnotationPresent(Primary.class),
                                        bean.initMethod() == null ? null : bean.initMethod(),
                                        bean.destroyMethod() == null ? null : bean.destroyMethod(),
                                        null,
                                        null);
                                logger.atDebug().log("Create bean definition by @Bean: {}", methodBeanDefinition);
                                beans.put(methodName, methodBeanDefinition);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new BeanDefinitionException("Cannot find class: " + beanClassName, e);
            }
        }
        return beans;
    }

    private int getOrder(Method method) {
        Order order = method.getAnnotation(Order.class);
        return order == null ? 0 : order.value();
    }

    private boolean isPrimary(Class<?> clazz) {
        return clazz.getAnnotation(Primary.class) != null;
    }

    private int getOrder(Class<?> clazz) {
        Order order = clazz.getAnnotation(Order.class);
        return order == null ? 0 : order.value();
    }

    Constructor<?> getConstructor(Class<?> clazz) {

        Constructor<?>[] constructors = clazz.getConstructors();
        // 没有public的构造方法，就找所有的构造方法
        if (constructors.length == 0) {
            constructors = clazz.getDeclaredConstructors();
            if (constructors.length == 0) {
                throw new BeanDefinitionException("No constructor found for class: " + clazz.getName());
            }
            if (constructors.length > 1) {
                throw new BeanDefinitionException("Multiple constructors found for class: " + clazz.getName());
            }
        }
        // 只有一个构造方法，直接返回
        if (constructors.length == 1) {
            return constructors[0];
        } else {
            // 有多个构造方法，找到标记了Primary的构造方法
            Constructor<?> primaryConstructor = null;
            for (Constructor<?> constructor : constructors) {
                if (constructor.getAnnotation(Primary.class) != null) {
                    if (primaryConstructor != null) {
                        throw new BeanDefinitionException("Multiple primary constructors found for class: " + clazz.getName());
                    }
                    primaryConstructor = constructor;
                }
            }
            if (primaryConstructor != null) {
                return primaryConstructor;
            }
            // 没有标记Primary的构造方法，抛出异常，因为不知道这个bean到底用哪个构造函数可以构造（这个应该算是没实现好）
            throw new BeanDefinitionException("No primary constructor found for class: " + clazz.getName());
        }
    }

    public BeanDefinition findBeanDefinition(String beanName) {
        return beans.get(beanName);
    }

    public List<BeanDefinition> findBeanDefinitions(Class<?> clazz) {
        return this.beans.values().stream()
                // filter by type and sub-type:
                .filter(def -> clazz.isAssignableFrom(def.getBeanClass()))
                // 排序:
                .sorted().collect(Collectors.toList());
    }

    public BeanDefinition findBeanDefinition(Class<?> clazz) {
        List<BeanDefinition> defs = findBeanDefinitions(clazz);
        if (defs.isEmpty()) {
            return null;
        }
        if (defs.size() == 1) {
            return defs.getFirst();
        }
        List<BeanDefinition> list = defs.stream().filter(BeanDefinition::isPrimary).toList();
        if (list.size() > 1) {
            throw new BeanDefinitionException("Multiple primary beans found for class: " + clazz.getName());
        }
        if (list.isEmpty()) {
            throw new BeanDefinitionException("No primary bean found for class: " + clazz.getName());
        }
        return list.getFirst();
    }
}
