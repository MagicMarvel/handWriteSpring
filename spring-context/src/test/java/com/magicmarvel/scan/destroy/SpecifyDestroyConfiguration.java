package com.magicmarvel.scan.destroy;


import org.magicmarvel.spring.context.annotation.Bean;
import org.magicmarvel.spring.context.annotation.Configuration;
import org.magicmarvel.spring.context.annotation.Value;

@Configuration
public class SpecifyDestroyConfiguration {

    @Bean(destroyMethod = "destroy")
    SpecifyDestroyBean createSpecifyDestroyBean(@Value("${app.title}") String appTitle) {
        return new SpecifyDestroyBean(appTitle);
    }
}
