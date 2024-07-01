package com.magicmarvel.scan.destroy;


import org.magicmarvel.handWriteSpring.annotation.Bean;
import org.magicmarvel.handWriteSpring.annotation.Configuration;
import org.magicmarvel.handWriteSpring.annotation.Value;

@Configuration
public class SpecifyDestroyConfiguration {

    @Bean(destroyMethod = "destroy")
    SpecifyDestroyBean createSpecifyDestroyBean(@Value("${app.title}") String appTitle) {
        return new SpecifyDestroyBean(appTitle);
    }
}
