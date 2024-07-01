package com.magicmarvel.scan.init;


import org.magicmarvel.handWriteSpring.annotation.Bean;
import org.magicmarvel.handWriteSpring.annotation.Configuration;
import org.magicmarvel.handWriteSpring.annotation.Value;

@Configuration
public class SpecifyInitConfiguration {

    @Bean(initMethod = "init")
    SpecifyInitBean createSpecifyInitBean(@Value("${app.title}") String appTitle, @Value("${app.version}") String appVersion) {
        return new SpecifyInitBean(appTitle, appVersion);
    }
}
