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
 * 客户端需要提供一个类，继承该抽象类，会自动创建BeanPostProcessor，并查找给定注解的value值，以找到InvocationHandler
 *
 * @param <A> 需要创建BeanPostProcessor的注解
 */
public abstract class AnnotationProxyBeanPostProcessor<A extends Annotation> implements BeanPostProcessor {

    private final ProxyResolver proxyResolver = new ProxyResolver();
    private final Class<A> type;

    public AnnotationProxyBeanPostProcessor() {
        this.type = getParameterizedType();
    }

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
     * 获取泛型（牛逼PLUS，没这么写过）
     *
     * @return 泛型的类类型
     */
    private Class<A> getParameterizedType() {
        Type type = getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType pt)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type.");
        }
        Type[] types = pt.getActualTypeArguments();
        if (types.length != 1) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " has more than 1 parameterized types.");
        }
        Type r = types[0];
        if (!(r instanceof Class<?>)) {
            throw new IllegalArgumentException("Class " + getClass().getName() + " does not have parameterized type of class.");
        }
        return (Class<A>) r;
    }

}
