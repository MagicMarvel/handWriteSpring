package org.magicmarvel.spring.context.context;


import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.magicmarvel.spring.context.annotation.*;
import org.magicmarvel.spring.context.exception.*;
import org.magicmarvel.spring.context.io.property.PropertyResolver;
import org.magicmarvel.spring.context.io.resource.ResourceResolver;
import org.magicmarvel.spring.context.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;


public class AnnotationConfigApplicationContext implements ConfigurableApplicationContext, ApplicationContext {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PropertyResolver propertyResolver;
    private final Map<String, BeanDefinition> beans = new HashMap<>();
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    Set<String> creatingBeanNames = new HashSet<>();


    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) throws URISyntaxException, IOException {

        ApplicationContextUtils.setApplicationContext(this);

        this.propertyResolver = propertyResolver;

        // 扫描获取所有Bean的Class类型:
        final Set<String> beanClassNames = scanForClassNames(configClass);

        // 创建Bean的定义
        createBeanDefinitions(beanClassNames);

        // 实例化beans
        createBeanInstances();

        // 注入bean上字段的值，注入bean里函数的入参
        injectBeans();

        // 执行bean上的@PostConstruct方法
        invokeInitMethods();
    }

    private void invokeInitMethods() {
        for (BeanDefinition def : beans.values()) {
            if (def.getInitMethod() != null) {
                try {
                    def.getInitMethod().invoke(def.getInstance());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BeanCreationException("Cannot invoke @PostConstruct method: " + def.getInitMethodName(), e);
                }
            }
        }
    }

    private void injectBeans() {
        for (BeanDefinition def : beans.values()) {
            if (def.getInstance() != null) {
                injectBean(def.getOriginInstance(), def.getBeanClass());
            } else {
                throw new BeanCreationException("Bean instance not found: " + def.getName());
            }
        }
    }

    /**
     * 判断needScanClass里面有没有字段是需要注入的（如带有@Autowired、@Value的字段）
     * 如果有，就注入到nowInstance里，然后递归处理父类
     *
     * @param nowInstance   当前实例
     * @param needScanClass 需要扫描的类
     */
    private void injectBean(Object nowInstance, Class<?> needScanClass) {
        // 处理字段注入
        for (Field field : needScanClass.getDeclaredFields()) {
            // 处理一个类的字段AutoWired注入
            if (field.isAnnotationPresent(Autowired.class)) {
                Class<?> type = field.getType();
                BeanDefinition refBean = findBeanDefinition(type);
                if (refBean == null) {
                    throw new UnsatisfiedDependencyException("No bean found for type: " + type.getName());
                }
                if (refBean.getInstance() == null) {
                    throw new UnsatisfiedDependencyException("No bean instance found for type: " + type.getName());
                }
                try {
                    field.setAccessible(true);
                    field.set(nowInstance, refBean.getInstance());
                    logger.atDebug().log("Inject Bean {} field {} with @Autowired: {}", nowInstance, field.getName(), field.get(nowInstance));
                } catch (IllegalAccessException e) {
                    throw new BeanCreationException("Cannot inject field: " + field.getName(), e);
                }
            }
            // 处理类字段的Value注入
            if (field.isAnnotationPresent(Value.class)) {
                Value value = field.getAnnotation(Value.class);
                try {
                    field.setAccessible(true);
                    field.set(nowInstance, propertyResolver.getProperty(value.value(), field.getType()));
                    logger.atDebug().log("Inject Bean {} field {} with @Value: {}", nowInstance, field.getName(), field.get(nowInstance));
                } catch (IllegalAccessException e) {
                    throw new BeanCreationException("Cannot inject field: " + field.getName(), e);
                }
            }
        }

        // 处理setter函数注入
        for (Method method : needScanClass.getDeclaredMethods()) {
            if (method.getReturnType() == void.class && method.getParameters().length == 1
                    && method.getName().startsWith("set") && method.isAnnotationPresent(Value.class)) {
                String param = propertyResolver.getProperty(method.getAnnotation(Value.class).value());
                try {
                    method.invoke(nowInstance, param);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BeanCreationException(e);
                }
            }
        }


        if (needScanClass.getSuperclass() != Object.class) {
            injectBean(nowInstance, needScanClass.getSuperclass());
        }
    }

    /**
     * 实例化所有的Bean，先实例化Configuration的Bean，再创建剩余的没有实例化的bean
     */
    private void createBeanInstances() {
        // 1. 把所有的Configuration拿出来先实例化他们，不然他们的工厂方法bean没法实例化
        beans.values().stream().filter(BeanDefinition::isConfigurator).sorted().forEach(this::createBeanAsEarlySingleton);

        // 2. 创建所有BeanPostProcessor的定义
        beans.values().stream().filter(BeanDefinition::isBeanPostProcessor).sorted().forEach(bean -> {
            createBeanAsEarlySingleton(bean);
            beanPostProcessors.add((BeanPostProcessor) bean.getInstance());
        });

        // 3. 把所有的bean都实例化出来
        // 这里会执行BeanPostProcessor，有些BeanPostProcessor可能会手动创建其他的bean实例用来调用的
        // 注意这个包的测试用例 com.magicmarvel.spring.aop.metric
        beans.values().stream().filter(bean -> bean.getInstance() == null).sorted().forEach(this::createBeanAsEarlySingleton);
    }

    /**
     * 创建Bean的实例，可以重复调用，如果之前创建过了，会返回之前创建的实例
     * <Br/>
     * 如果有循环依赖，会抛出异常
     * <Br/>
     * 最终的实例会直接放到BeanDefinition里，也会直接返回出来
     * <Br/>
     * 代理前的实例（和BeanPostProcessor有关那种）会放在BeanDefinition的originInstance里
     *
     * @param bean Bean的定义
     * @return Bean的实例
     */
    @Override
    public Object createBeanAsEarlySingleton(BeanDefinition bean) {
        try {
            if (bean.getInstance() != null) {
                return bean.getInstance();
            }
            if (this.creatingBeanNames.contains(bean.getName())) {
                throw new UnsatisfiedDependencyException("Circular dependency: " + bean.getName());
            }

            Executable createFn = bean.getFactoryName() == null ?
                    bean.getConstructor() : bean.getFactoryMethod();
            if (createFn == null) {
                throw new BeanCreationException("Cannot create " + bean.getName() + " instance cause it doesn't have constructor or factory method");
            }


            Object[] params = new Object[createFn.getParameterCount()];
            Parameter[] parameters = createFn.getParameters();
            for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
                Parameter parameter = parameters[i];
                // 在工厂方法返回一个Bean的时候，工厂方法的入参不需要AutoWired注解，Spring也应该找对应的注解注入进去
                // 构造函数创建Bean的时候，也是适用的，不需要入参有AutoWired就能注入
                if (parameter.isAnnotationPresent(Autowired.class) || parameter.getAnnotations().length == 0) {
                    Class<?> type = parameter.getType();
                    BeanDefinition paramBean = findBeanDefinition(type);
                    if (paramBean == null) {
                        throw new UnsatisfiedDependencyException("No bean found for type: " + type.getName());
                    }
                    Object paramBeanInstance = paramBean.getInstance();
                    if (paramBeanInstance == null) {
                        paramBeanInstance = createBeanAsEarlySingleton(paramBean);
                    }
                    params[i] = paramBeanInstance;
                }
                if (parameter.isAnnotationPresent(Value.class)) {
                    Value value = parameter.getAnnotation(Value.class);
                    params[i] = propertyResolver.getProperty(value.value(), parameter.getType());
                }
            }

            Object instance = null;
            if (bean.getConstructor() != null) {
                Constructor<?> constructor = bean.getConstructor();
                constructor.setAccessible(true);
                instance = constructor.newInstance(params);
            }

            if (bean.getFactoryMethod() != null) {
                Object originBean = this.getBean(bean.getFactoryMethod().getDeclaringClass());
                Method factoryMethod = bean.getFactoryMethod();
                factoryMethod.setAccessible(true);
                instance = factoryMethod.invoke(originBean, params);
            }

            bean.setOriginInstance(instance);

            // 调用BeanPostProcessor处理Bean:
            for (BeanPostProcessor processor : beanPostProcessors) {
                instance = processor.postProcessBeforeInitialization(instance, bean.getName());
            }
            bean.setInstance(instance);
            return bean.getInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new BeanCreationException(e);
        }
    }

    /**
     * 针对给定的起点class，扫描他的ComponentScan注解，扫描注解里指定的包路径，获取所有Bean的包路径，待会用getName方法获取Class
     *
     * @param configClass 起点class
     * @return Bean的包路径集合
     */
    private Set<String> scanForClassNames(Class<?> configClass) throws URISyntaxException, IOException {
        // 要在哪个包里扫描
        String[] scanPackages;

        // 获取ComponentScan注解，如果没有则默认扫描当前包路径
        ComponentScan componentScanAnno = ClassUtils.findAnnotation(configClass, ComponentScan.class);
        if (componentScanAnno == null || componentScanAnno.value().length == 0) {
            scanPackages = new String[]{configClass.getPackageName()};
        } else {
            scanPackages = componentScanAnno.value();
        }
        logger.atDebug().log("Scan packages in: {}", Arrays.toString(scanPackages));

        // 所有的要被框架管理的bean的包路径
        Set<String> classNameSet = findAllClassFile(scanPackages);

        // 处理Import注解，Import注解里加的类对象都要被框架管理
        Import importAnno = ClassUtils.findAnnotation(configClass, Import.class);
        if (importAnno != null) {
            for (Class<?> importClass : importAnno.value()) {
                classNameSet.add(importClass.getName());
            }
        }
        return classNameSet;
    }

    /**
     * 扫描指定包路径下的所有class文件
     *
     * @param scanPackages 包路径
     * @return 所有class文件的包路径
     * @throws URISyntaxException URL -> URI 转换错误
     * @throws IOException        读取文件错误
     */
    private Set<String> findAllClassFile(String[] scanPackages) throws URISyntaxException, IOException {
        Set<String> classNameSet = new HashSet<>();

        for (String scanPackage : scanPackages) {
            ResourceResolver resourceResolver = new ResourceResolver(scanPackage);
            List<String> scanClasses = resourceResolver.scan(resource -> {
                if (resource.name().endsWith(".class")) {
                    String className = resource.name()
                            .replace("/", ".")
                            .replace("\\", ".")
                            .substring(0, resource.name().length() - 6);
                    logger.atDebug().log("Found class: {}", className);
                    return className;
                }
                return null;
            });
            classNameSet.addAll(scanClasses);
        }
        return classNameSet;
    }

    /**
     * 创建Bean的定义
     *
     * @param beanClassNames Bean的类名集合，这里类名指的是"annotation.org.magicmarvel.context.context.AliasFor"这种很长很全的
     */
    private void createBeanDefinitions(Set<String> beanClassNames) {
        for (String beanClassName : beanClassNames) {
            try {
                Class<?> beanClass = Class.forName(beanClassName);
                // 找到所有标记了Component的类，这些类都是Bean，Configuration也是Component
                if (ClassUtils.findAnnotation(beanClass, Component.class) != null &&
                        !beanClass.isAnnotation() &&
                        !beanClass.isInterface() &&
                        !beanClass.isEnum() &&
                        !beanClass.isPrimitive()
                ) {
                    String beanName = ClassUtils.getBeanName(beanClass);
                    Method postConstructMethod = ClassUtils.findAnnotationMethod(beanClass, PostConstruct.class);
                    Method preDestroyMethod = ClassUtils.findAnnotationMethod(beanClass, PreDestroy.class);
                    BeanDefinition beanDefinition = new BeanDefinition(beanName,
                            beanClass,
                            getConstructor(beanClass),
                            getOrder(beanClass),
                            isPrimary(beanClass),
                            postConstructMethod == null ? null : postConstructMethod.getName(),
                            preDestroyMethod == null ? null : preDestroyMethod.getName(),
                            postConstructMethod,
                            preDestroyMethod);
                    logger.atDebug().log("Create bean definition by @Component: {}", beanDefinition);
                    beans.put(beanName, beanDefinition);


                    // 如果是Configuration，还要找到所有的标记为Bean的方法
                    if (beanClass.isAnnotationPresent(Configuration.class)) {
                        for (Method method : beanClass.getDeclaredMethods()) {
                            if (method.isAnnotationPresent(Bean.class)) {
                                String methodName = ClassUtils.getBeanName(method);
                                Bean bean = method.getAnnotation(Bean.class);
                                String initMethodName = bean.initMethod();
                                String destroyMethodName = bean.destroyMethod();
                                Method initMethod = null;
                                if (initMethodName != null && !initMethodName.isEmpty()) {
                                    initMethod = method.getReturnType().getDeclaredMethod(initMethodName);
                                }
                                Method destroyMethod = null;
                                if (destroyMethodName != null && !destroyMethodName.isEmpty()) {
                                    destroyMethod = method.getReturnType().getDeclaredMethod(destroyMethodName);
                                }
                                BeanDefinition methodBeanDefinition = new BeanDefinition(
                                        methodName,
                                        method.getReturnType(),
                                        beanName,
                                        method,
                                        getOrder(method),
                                        method.isAnnotationPresent(Primary.class),
                                        initMethodName,
                                        destroyMethodName,
                                        initMethod,
                                        destroyMethod);
                                logger.atDebug().log("Create bean definition by @Bean: {}", methodBeanDefinition);
                                beans.put(methodName, methodBeanDefinition);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new BeanDefinitionException("Cannot find class: " + beanClassName, e);
            } catch (NoSuchMethodException e) {
                throw new BeanDefinitionException("Cannot find method", e);
            }
        }
    }

    /**
     * 获取方法的Order注解的值，没有就返回0，这个字段用于在Configuration类里对所有的方法创建bean时的顺序进行排序
     *
     * @param method 方法
     * @return Order注解的值
     */
    private int getOrder(Method method) {
        Order order = method.getAnnotation(Order.class);
        return order == null ? 0 : order.value();
    }

    /**
     * 判断一个类是否是Primary，当一个字段可以注入多个对象的时候（比如一个父类有好几个子类，挑选一个子类注入），Primary总是优先被注入
     *
     * @param clazz 类
     * @return 是否是Primary
     */
    private boolean isPrimary(Class<?> clazz) {
        return clazz.getAnnotation(Primary.class) != null;
    }

    /**
     * 获取类的Order注解的值，没有就返回0
     *
     * @param clazz 类
     * @return Order注解的值
     */
    private int getOrder(Class<?> clazz) {
        Order order = clazz.getAnnotation(Order.class);
        return order == null ? 0 : order.value();
    }

    /**
     * 获取一个类的构造方法，如果有多个构造方法，会优先选择标记了Primary的构造方法，如果没有标记Primary的构造方法，会抛出异常
     *
     * @param clazz 类
     * @return 构造方法
     */
    Constructor<?> getConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        // 没有public的构造方法，就找所有的构造方法
        if (constructors.length == 0) {
            constructors = clazz.getDeclaredConstructors();
            if (constructors.length == 0) {
                throw new BeanDefinitionException("No constructor found for class: " + clazz.getName());
            }
            if (constructors.length > 1) {
                throw new BeanDefinitionException("Multiple constructors found for class: " + clazz.getName());
            }
        }
        // 只有一个构造方法，直接返回
        if (constructors.length == 1) {
            return constructors[0];
        } else {
            // 有多个构造方法，找到标记了Primary的构造方法
            Constructor<?> primaryConstructor = null;
            for (Constructor<?> constructor : constructors) {
                if (constructor.getAnnotation(Primary.class) != null) {
                    if (primaryConstructor != null) {
                        throw new BeanDefinitionException("Multiple primary constructors found for class: " + clazz.getName());
                    }
                    primaryConstructor = constructor;
                }
            }
            if (primaryConstructor != null) {
                return primaryConstructor;
            }
            // 没有标记Primary的构造方法，抛出异常，因为不知道这个bean到底用哪个构造函数可以构造（这个应该算是没实现好）
            throw new BeanDefinitionException("No primary constructor found for class: " + clazz.getName());
        }
    }

    /**
     * 根据beanName获取Bean
     *
     * @param beanName Bean的名字
     * @return Bean
     */
    @Nullable
    @Override
    public BeanDefinition findBeanDefinition(String beanName) {
        return beans.get(beanName);
    }

    @Nullable
    @Override
    public BeanDefinition findBeanDefinition(String name, Class<?> requiredType) {
        BeanDefinition def = findBeanDefinition(name);
        if (def == null) {
            return null;
        }
        if (!requiredType.isAssignableFrom(def.getBeanClass())) {
            throw new BeanNotOfRequiredTypeException(
                    String.format("Autowire required type '%s' but bean '%s' has actual type '%s'.", requiredType.getName(),
                            name, def.getBeanClass().getName()));
        }
        return def;
    }

    /**
     * 根据类获取Bean的List，所有满足条件的都会被找出来（clazz的子类也会被找出来）
     *
     * @param clazz 类
     * @return Bean的List
     */
    @Override
    public List<BeanDefinition> findBeanDefinitions(Class<?> clazz) {
        return this.beans.values().stream()
                .filter(def ->
                        clazz.isAssignableFrom(def.getBeanClass())
                )
                .sorted().collect(Collectors.toList());
    }

    /**
     * 根据类获取Bean，如果有多个Bean，会优先选择Primary的Bean，如果没有Primary的Bean，会抛出异常
     *
     * @param clazz 类
     * @return Bean
     */
    @Override
    public @Nullable BeanDefinition findBeanDefinition(Class<?> clazz) {
        List<BeanDefinition> defs = findBeanDefinitions(clazz);
        if (defs.isEmpty()) {
            return null;
        }
        if (defs.size() == 1) {
            return defs.getFirst();
        }
        List<BeanDefinition> list = defs.stream().filter(BeanDefinition::isPrimary).toList();
        if (list.size() > 1) {
            throw new BeanDefinitionException("Multiple primary beans found for class: " + clazz.getName());
        }
        if (list.isEmpty()) {
            throw new BeanDefinitionException("No primary bean found for class: " + clazz.getName());
        }
        return list.getFirst();
    }

    @Override
    public boolean containsBean(String name) {
        return beans.containsKey(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(name, requiredType);
        if (def != null) {
            return requiredType.cast(def.getInstance());
        } else {
            return null;
        }
    }

    public <T> T getBean(Class<T> clazz) {
        Object instance = Objects.requireNonNull(findBeanDefinition(clazz)).getInstance();
        return clazz.cast(instance);  // 使用 Class.cast() 来进行类型转换
    }

    @Override
    public <T> List<T> getBeans(Class<T> requiredType) {
        List<BeanDefinition> defs = findBeanDefinitions(requiredType);
        if (!defs.isEmpty()) {
            return defs.stream().map(def -> requiredType.cast(def.getInstance())).collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public void close() {
        logger.info("Closing {}...", this.getClass().getName());
        this.beans.values().forEach(def -> {
            final Object beanInstance = def.getInstance();
            if (def.getDestroyMethod() != null) {
                try {
                    def.getDestroyMethod().invoke(beanInstance);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        this.beans.clear();
        logger.info("{} closed.", this.getClass().getName());
        ApplicationContextUtils.setApplicationContext(null);
    }

    public <T> T getBean(String className) {
        BeanDefinition beanDefinition = findBeanDefinition(className);
        if (beanDefinition == null) {
            throw new NoSuchBeanDefinitionException("No bean found for name: " + className);
        }
        return (T) beanDefinition.getInstance();
    }
}
