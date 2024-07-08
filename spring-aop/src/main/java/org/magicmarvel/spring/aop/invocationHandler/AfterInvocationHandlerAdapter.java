package org.magicmarvel.spring.aop.invocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class AfterInvocationHandlerAdapter implements InvocationHandler {

    // 操，这他妈是什么操作？我还真没想到能这样写
    public abstract Object after(Object proxy, Object returnValue, Method method, Object[] args);

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = method.invoke(proxy, args);
        return after(proxy, ret, method, args);
    }
}
