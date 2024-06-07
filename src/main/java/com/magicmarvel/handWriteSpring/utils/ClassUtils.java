package com.magicmarvel.handWriteSpring.utils;

import java.lang.annotation.Annotation;

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
}
