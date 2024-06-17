package com.magicmarvel.handWriteSpring.utils;

import com.magicmarvel.handWriteSpring.annotation.AliasFor;
import com.magicmarvel.handWriteSpring.annotation.Bean;
import com.magicmarvel.handWriteSpring.annotation.Component;
import com.magicmarvel.handWriteSpring.exception.BeanDefinitionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ClassUtils {
    public static <T extends Annotation> T findAnnotation(Class<?> target, Class<T> annoClass) {
        T found = target.getAnnotation(annoClass);
        for (Annotation anno : target.getAnnotations()) {
            Class<? extends Annotation> annotationType = anno.annotationType();
            // Java自带的注解就不扫描了
            if (!annotationType.getPackageName().equals("java.lang.annotation")) {
                T findFromAnnotation = findAnnotation(annotationType, annoClass);
                if (findFromAnnotation != null) {
                    if (found != null) {
                        throw new BeanDefinitionException("Duplicate @" + annoClass.getSimpleName() + " found on class " + target.getSimpleName());
                    }
                    found = findFromAnnotation;
                }
            }
        }
        return found;
    }

    /**
     * 获取Bean的名称，这里传入的class一定是有Component的class，不过有可能是其他注解里面的Component
     * 如果有有AliasFor字段的注解在头上，则解析那个注解，看看是否是Component的value字段的别名
     * 否则默认为类名
     * TODO: 递归解析AliasFor字段
     *
     * @param clazz Bean的Class
     * @return Bean的名称
     */
    public static String getBeanName(Class<?> clazz) {
        Component annotation = clazz.getAnnotation(Component.class);
        // 如果有Component注解
        if (annotation != null) {
            return annotation.value().isEmpty()
                    ? Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1)
                    : annotation.value();
        } else {
            // 如果没有Component注解，看看有没有其他注解，包含了Component注解，还用AliasFor注解指向了Component
            for (Annotation clazzAnnotation : clazz.getAnnotations()) {
                try {
                    String beanName = getBeanNameByAnnotation(clazzAnnotation);
                    if (beanName != null) {
                        return beanName;
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1);
    }

    /**
     * 如果有AliasFor字段的注解在头上，则解析那个注解，看看是否是Component的value字段的别名
     *
     * @param annotation
     * @return
     */
    private static String getBeanNameByAnnotation(Annotation annotation) throws InvocationTargetException, IllegalAccessException {
        Class<? extends Annotation> aClass = annotation.annotationType();
        if (findAnnotation(aClass, Component.class) != null) {
            for (Method method : aClass.getMethods()) {
                if (method.isAnnotationPresent(AliasFor.class)) {
                    AliasFor aliasFor = method.getAnnotation(AliasFor.class);
                    if (aliasFor.annotation().equals(Component.class) && Objects.equals(aliasFor.attribute(), "value")) {
                        String methodValue = (String) method.invoke(annotation);
                        if (!Objects.equals(methodValue, "")) {
                            return methodValue;
                        }
                    }
                }
            }
        }
        return null;
    }


    public static <T extends Annotation> Method findAnnotationMethod(Class<?> clazz, Class<T> annoClass) {
        Method found = null;
        for (Method method : clazz.getMethods()) {
            if (found == null) {
                if (method.getAnnotation(annoClass) != null) {
                    found = method;
                }
            } else {
                if (method.getAnnotation(annoClass) != null) {
                    throw new RuntimeException("Duplicate @" + annoClass.getSimpleName() + " found on class " + clazz.getSimpleName());
                }
            }
        }
        return found;
    }

    public static String getBeanName(Method method) {
        Bean bean = method.getAnnotation(Bean.class);
        return bean.value().isEmpty() ? method.getName() : bean.value();
    }
}
