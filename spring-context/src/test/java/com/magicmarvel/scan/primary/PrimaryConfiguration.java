package com.magicmarvel.scan.primary;


import org.magicmarvel.spring.context.annotation.Bean;
import org.magicmarvel.spring.context.annotation.Configuration;
import org.magicmarvel.spring.context.annotation.Primary;

@Configuration
public class PrimaryConfiguration {

    @Primary
    @Bean
    DogBean husky() {
        return new DogBean("Husky");
    }

    @Bean
    DogBean teddy() {
        return new DogBean("Teddy");
    }
}
