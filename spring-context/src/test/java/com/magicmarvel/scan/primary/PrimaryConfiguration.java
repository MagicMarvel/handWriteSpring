package com.magicmarvel.scan.primary;


import org.magicmarvel.handWriteSpring.annotation.Bean;
import org.magicmarvel.handWriteSpring.annotation.Configuration;
import org.magicmarvel.handWriteSpring.annotation.Primary;

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
