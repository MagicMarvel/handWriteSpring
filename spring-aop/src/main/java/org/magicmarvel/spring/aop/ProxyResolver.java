package org.magicmarvel.spring.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;

public class ProxyResolver {
    // ByteBuddy实例:
    ByteBuddy byteBuddy = new ByteBuddy();

    /**
     * 创建动态代理对象
     *
     * @param bean    要被代理的对象
     * @param handler 代理处理器（指示如何代理）
     * @return 被代理后的对象
     */
    public <T> T createProxy(T bean, InvocationHandler handler) {
        // 目标Bean的Class类型:
        Class<?> targetClass = bean.getClass();
        // 动态创建Proxy的Class:
        Class<?> proxyClass = this.byteBuddy
                // 子类用默认无参数构造方法:
                .subclass(targetClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                // 拦截所有public方法:
                .method(ElementMatchers.isPublic())
                .intercept(InvocationHandlerAdapter.of(
                        // 新的拦截器实例:
                        (proxy, method, args) -> {
                            // 将方法调用代理至原始Bean:
                            return handler.invoke(bean, method, args);
                        }))
                // 生成字节码:
                .make()
                // 加载字节码:
                .load(targetClass.getClassLoader()).getLoaded();
        // 创建Proxy实例:
        Object proxy;
        try {
            proxy = proxyClass.getConstructor().newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T) proxy;
    }

}
