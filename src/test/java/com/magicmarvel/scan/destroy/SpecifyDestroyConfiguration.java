package com.magicmarvel.scan.destroy;

import com.magicmarvel.handWriteSpring.annotation.Bean;
import com.magicmarvel.handWriteSpring.annotation.Configuration;
import com.magicmarvel.handWriteSpring.annotation.Value;

@Configuration
public class SpecifyDestroyConfiguration {

    @Bean(destroyMethod = "destroy")
    SpecifyDestroyBean createSpecifyDestroyBean(@Value("${app.title}") String appTitle) {
        return new SpecifyDestroyBean(appTitle);
    }
}
