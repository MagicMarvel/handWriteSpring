package com.magicmarvel.imported;

import com.magicmarvel.handWriteSpring.annotation.Bean;
import com.magicmarvel.handWriteSpring.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class LocalDateConfiguration {

    @Bean
    LocalDate startLocalDate() {
        return LocalDate.now();
    }

    @Bean
    LocalDateTime startLocalDateTime() {
        return LocalDateTime.now();
    }
}
