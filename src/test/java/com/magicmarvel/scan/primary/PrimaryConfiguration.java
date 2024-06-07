package com.magicmarvel.scan.primary;

import com.magicmarvel.handWriteSpring.annotation.Bean;
import com.magicmarvel.handWriteSpring.annotation.Configuration;
import com.magicmarvel.handWriteSpring.annotation.Primary;

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
