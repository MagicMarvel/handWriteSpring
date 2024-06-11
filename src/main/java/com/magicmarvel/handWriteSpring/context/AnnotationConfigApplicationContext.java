package com.magicmarvel.handWriteSpring.context;

import com.magicmarvel.handWriteSpring.annotation.*;
import com.magicmarvel.handWriteSpring.exception.BeanDefinitionException;
import com.magicmarvel.handWriteSpring.io.property.PropertyResolver;
import com.magicmarvel.handWriteSpring.io.resource.ResourceResolver;
import com.magicmarvel.handWriteSpring.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;

public class AnnotationConfigApplicationContext {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PropertyResolver propertyResolver;
    private final Map<String, BeanDefinition> beans;

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
        for (String beanClassName : beanClassNames) {
            try {
                Class<?> beanClass = Class.forName(beanClassName);
                // 找到所有标记了Component的类，这些类都是Bean，Configuration也是Component
                if (ClassUtils.findAnnotation(beanClass, Component.class) != null) {
                    String beanName = ClassUtils.getBeanName(beanClass);
                    BeanDefinition beanDefinition = new BeanDefinition(beanName, beanClass, beanClass.getConstructor());


                    // 如果是Configuration，还要找到所有的标记为Bean的方法
                    if (ClassUtils.findAnnotation(beanClass, Configuration.class) != null) {
                        for (Method method : beanClass.getMethods()) {
                            if (method.getAnnotation(Bean.class) != null) {
                            }
                        }
                    }
                }
                String beanName = ClassUtils.getBeanName(beanClass);
                BeanDefinition beanDefinition = new BeanDefinition(beanName, beanClass);
            } catch (ClassNotFoundException e) {
                throw new BeanDefinitionException("Cannot find class: " + beanClassName, e);
            }
        }
        return null;
    }


    public BeanDefinition findBeanDefinition(String customAnnotation) {
        return null;
    }

    public BeanDefinition findBeanDefinition(Class<?> localDateConfigurationClass) {
        return null;
    }

    public List<BeanDefinition> findBeanDefinitions(Class<?> personBeanClass) {
        return null;
    }
}
