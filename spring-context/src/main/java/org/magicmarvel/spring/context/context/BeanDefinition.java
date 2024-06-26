package org.magicmarvel.spring.context.context;

import jakarta.annotation.Nullable;
import org.magicmarvel.spring.context.annotation.Configuration;
import org.magicmarvel.spring.context.exception.BeanCreationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Comparable注解用于表明这个对象应该怎么互相比较，类似于C++的重载< >运算符
 *
 * @author Keven
 */
public class BeanDefinition implements Comparable<BeanDefinition> {

    // 全局唯一的Bean Name
    private final String name;
    // Bean的声明类型（类Bean的类型就是类本身，用工厂方法弄出来的Bean的类型是工厂方法的返回类型，但是实际上方法有可能返回这个类的子类）
    private final Class<?> beanClass;
    // 构造方法，类Bean会有，工厂方法bean没有
    private final Constructor<?> constructor;
    // 工厂方法名称，工厂方法bean有，类bean没有
    private final String factoryName;
    // 工厂方法，工厂方法bean有，类bean没有
    private final Method factoryMethod;
    // Bean的顺序，类bean和工厂方法bean都有
    private final int order;
    // 是否标识@Primary，类bean和工厂方法bean都有
    private final boolean primary;
    // Bean的实例（如果这个bean被使用BeanPostProcessor替换了，则这里存放被替换成的bean）
    private Object instance = null;
    // Bean的原始实例（如果这个bean被使用BeanPostProcessor替换了，则这里存放原始的bean）
    private Object originInstance = null;

    // init and destroy method come from @Bean annotation
    // or come from @PostConstruct and @PreDestroy annotation in @Component
    private String initMethodName;
    private String destroyMethodName;
    private Method initMethod;
    private Method destroyMethod;

    // 用于@Component这样定义的bean
    public BeanDefinition(String name, Class<?> beanClass, Constructor<?> constructor, int order, boolean primary, String initMethodName,
                          String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.constructor = constructor;
        this.factoryName = null;
        this.factoryMethod = null;
        this.order = order;
        this.primary = primary;
        constructor.setAccessible(true);
        setInitAndDestroyMethod(initMethodName, destroyMethodName, initMethod, destroyMethod);
    }

    // 用于@Bean这样定义的bean
    public BeanDefinition(String name, Class<?> beanClass, String factoryName, Method factoryMethod, int order, boolean primary, String initMethodName,
                          String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.name = name;
        this.beanClass = beanClass;
        this.constructor = null;
        this.factoryName = factoryName;
        this.factoryMethod = factoryMethod;
        this.order = order;
        this.primary = primary;
        factoryMethod.setAccessible(true);
        setInitAndDestroyMethod(initMethodName, destroyMethodName, initMethod, destroyMethod);
    }

    private void setInitAndDestroyMethod(String initMethodName, String destroyMethodName, Method initMethod, Method destroyMethod) {
        this.initMethodName = initMethodName;
        this.destroyMethodName = destroyMethodName;
        if (initMethod != null) {
            initMethod.setAccessible(true);
        }
        if (destroyMethod != null) {
            destroyMethod.setAccessible(true);
        }
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
    }

    @Nullable
    public Constructor<?> getConstructor() {
        return this.constructor;
    }

    @Nullable
    public String getFactoryName() {
        return this.factoryName;
    }

    @Nullable
    public Method getFactoryMethod() {
        return this.factoryMethod;
    }

    @Nullable
    public Method getInitMethod() {
        return this.initMethod;
    }

    @Nullable
    public Method getDestroyMethod() {
        return this.destroyMethod;
    }

    @Nullable
    public String getInitMethodName() {
        return this.initMethodName;
    }

    @Nullable
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }

    public String getName() {
        return this.name;
    }

    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    @Nullable
    public Object getInstance() {
        return this.instance;
    }

    @Nullable
    public Object getOriginInstance() {
        return this.originInstance;
    }

    public void setInstance(Object instance) {
        Objects.requireNonNull(instance, "Bean instance is null.");
        if (!this.beanClass.isAssignableFrom(instance.getClass())) {
            throw new BeanCreationException(String.format("Instance '%s' of Bean '%s' is not the expected type: %s", instance, instance.getClass().getName(),
                    this.beanClass.getName()));
        }
        this.instance = instance;
    }

    public void setOriginInstance(Object originInstance) {
        Objects.requireNonNull(originInstance, "Bean originInstance is null.");
        if (!this.beanClass.isAssignableFrom(originInstance.getClass())) {
            throw new BeanCreationException(String.format("OriginInstance '%s' of Bean '%s' is not the expected type: %s", originInstance, originInstance.getClass().getName(),
                    this.beanClass.getName()));
        }
        this.originInstance = originInstance;
    }


    public Object getRequiredInstance() {
        if (this.instance == null) {
            throw new BeanCreationException(String.format("Instance of bean with name '%s' and type '%s' is not instantiated during current stage.",
                    this.getName(), this.getBeanClass().getName()));
        }
        return this.instance;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    @Override
    public String toString() {
        return "BeanDefinition [name=" + name + ", beanClass=" + beanClass.getName() + ", factory=" + getCreateDetail() + ", init-method="
                + (initMethod == null ? "null" : initMethod.getName()) + ", destroy-method=" + (destroyMethod == null ? "null" : destroyMethod.getName())
                + ", primary=" + primary + ", instance=" + instance + "]";
    }

    String getCreateDetail() {
        if (this.factoryMethod != null) {
            String params = String.join(", ", Arrays.stream(this.factoryMethod.getParameterTypes()).map(Class::getSimpleName).toArray(String[]::new));
            return this.factoryMethod.getDeclaringClass().getSimpleName() + "." + this.factoryMethod.getName() + "(" + params + ")";
        }
        return null;
    }

    public boolean isConfigurator() {
        return this.getBeanClass().isAnnotationPresent(Configuration.class);
    }

    public boolean isBeanPostProcessor() {
        return BeanPostProcessor.class.isAssignableFrom(this.getBeanClass());
    }

    @Override
    public int compareTo(BeanDefinition def) {
        int cmp = Integer.compare(this.order, def.order);
        if (cmp != 0) {
            return cmp;
        }
        return this.name.compareTo(def.name);
    }
}
