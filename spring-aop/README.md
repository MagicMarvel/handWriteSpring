需要使用AOP，客户端需要提供3个东西

- 一个自定义的注解，用来告诉容器哪个实例需要被代理
- 用自定义注解作为泛型继承`org.magicmarvel.spring.aop.beanPostProcessor.AnnotationProxyBeanPostProcessor`
  的类，这个类用来生成`BeanPostProcessor`来代理指定注解的bean实例，已经在框架里实现了，只需要继承即可
- 一个`java.lang.reflect.InvocationHandler`的实现类，用来告诉容器某个方法如何进行代理