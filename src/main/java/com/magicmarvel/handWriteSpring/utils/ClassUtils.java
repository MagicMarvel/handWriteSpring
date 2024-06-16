package com.magicmarvel.handWriteSpring.utils;

import com.magicmarvel.handWriteSpring.annotation.Bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

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
                        throw new RuntimeException("Duplicate @" + annoClass.getSimpleName() + " found on class " + target.getSimpleName());
                    }
                    found = findFromAnnotation;
                }
            }
        }
        return found;
    }

    public static String getBeanName(Class<?> simpleComponentClass) {
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
