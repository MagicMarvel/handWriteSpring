package org.magicmarvel.spring.aop.invocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class BeforeInvocationHandlerAdapter implements InvocationHandler {
    // 操，这他妈是什么操作？我还真没想到能这样写
    public abstract void before(Object proxy, Method method, Object[] args);

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before(proxy, method, args);
        return method.invoke(proxy, args);
    }
}
