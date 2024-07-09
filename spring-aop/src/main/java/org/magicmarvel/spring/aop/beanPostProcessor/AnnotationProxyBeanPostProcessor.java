package org.magicmarvel.spring.aop.beanPostProcessor;

import org.magicmarvel.spring.aop.ProxyResolver;
import org.magicmarvel.spring.context.context.ApplicationContextUtils;
import org.magicmarvel.spring.context.context.BeanDefinition;
import org.magicmarvel.spring.context.context.BeanPostProcessor;
import org.magicmarvel.spring.context.context.ConfigurableApplicationContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Abstract class designed to automatically create a {@link BeanPostProcessor} that searches for beans annotated with a specific annotation.
 * It then uses an {@link InvocationHandler} found by the annotation's value to create a proxy for the bean.
 * Subclasses must specify the annotation type they are interested in by extending this class with the annotation type as the generic parameter.
 *
 * @param <A> The annotation type used to identify beans that should be processed by this {@link BeanPostProcessor}.
 */
public abstract class AnnotationProxyBeanPostProcessor<A extends Annotation> implements BeanPostProcessor {

    private final ProxyResolver proxyResolver = new ProxyResolver();
    private final Class<A> type;

    /**
     * Constructor that initializes the {@link AnnotationProxyBeanPostProcessor} by determining the annotation type {@code A}.
     * This is achieved by inspecting the generic type parameter of the subclass.
     */
    public AnnotationProxyBeanPostProcessor() {
        this.type = getParameterizedType();
    }

    /**
     * Processes a bean before its initialization phase, searching for the specified annotation.
     * If the annotation is present, it attempts to create a proxy for the bean using an {@link InvocationHandler} specified by the annotation's value.
     *
     * @param bean     The bean instance to process.
     * @param beanName The name of the bean.
     * @return The original bean or a proxy of the bean if the specified annotation is present and an {@link InvocationHandler} can be found.
     * @throws NoSuchMethodException     If the annotation's value method cannot be found.
     * @throws InvocationTargetException If the annotation's value method cannot be invoked.
     * @throws IllegalAccessException    If the annotation's value method is inaccessible.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Annotation anno = bean.getClass().getAnnotation(type);
        if (anno != null) {
            ConfigurableApplicationContext ctx = ApplicationContextUtils.getRequiredConfigurableApplicationContext();
            String value = anno.annotationType().getMethod("value").invoke(anno).toString();
            BeanDefinition def = ctx.findBeanDefinition(value, InvocationHandler.class);// ensure the bean exists
            if (def == null) {
                throw new RuntimeException("InvocationHandler not found: " + value);
            }
            if (def.getInstance() == null) {
                try {
                    ctx.createBeanAsEarlySingleton(def);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            return proxyResolver.createProxy(bean, (InvocationHandler) def.getInstance());
        }
        return bean;

    }

    /**
     * Determines the class type of the annotation {@code A} by inspecting the generic type parameter of the subclass.
     * This method uses reflection to inspect the superclass's type parameter and casts it to {@code Class<A>}.
     *
     * @return The class type of the annotation {@code A}.
     * @throws IllegalArgumentException If the subclass does not specify a generic type parameter or if the parameter is not a class type.
     */
    private Class<A> getParameterizedType() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType parameterizedType)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " must be parameterized with an annotation type");
        }
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length != 1) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " must have exactly one generic type parameter");
        }
        if (!(typeArguments[0] instanceof Class)) {
            throw new IllegalArgumentException("Type argument " + typeArguments[0] + " is not a class");
        }
        return (Class<A>) typeArguments[0];
    }
}