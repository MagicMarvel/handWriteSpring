package com.magicmarvel.spring.aop.after;


import org.magicmarvel.spring.aop.beanPostProcessor.AroundProxyBeanPostProcessor;
import org.magicmarvel.spring.context.annotation.Bean;
import org.magicmarvel.spring.context.annotation.ComponentScan;
import org.magicmarvel.spring.context.annotation.Configuration;

@Configuration
@ComponentScan
public class AfterApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor() {
        return new AroundProxyBeanPostProcessor();
    }
}
