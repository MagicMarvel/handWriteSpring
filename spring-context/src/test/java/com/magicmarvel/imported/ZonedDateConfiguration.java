package com.magicmarvel.imported;


import org.magicmarvel.spring.context.annotation.Bean;
import org.magicmarvel.spring.context.annotation.Configuration;

import java.time.ZonedDateTime;

@Configuration
public class ZonedDateConfiguration {

    @Bean
    ZonedDateTime startZonedDateTime() {
        return ZonedDateTime.now();
    }
}
