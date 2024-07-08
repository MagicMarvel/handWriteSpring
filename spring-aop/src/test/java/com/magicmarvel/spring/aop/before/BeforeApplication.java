package com.magicmarvel.spring.aop.before;


import org.magicmarvel.spring.aop.beanPostProcessor.AroundProxyBeanPostProcessor;
import org.magicmarvel.spring.context.annotation.Bean;
import org.magicmarvel.spring.context.annotation.ComponentScan;
import org.magicmarvel.spring.context.annotation.Configuration;

@Configuration
@ComponentScan
public class BeforeApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor() {
        return new AroundProxyBeanPostProcessor();
    }
}
