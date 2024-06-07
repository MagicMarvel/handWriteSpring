package com.magicmarvel.imported;

import com.magicmarvel.handWriteSpring.annotation.Bean;
import com.magicmarvel.handWriteSpring.annotation.Configuration;

import java.time.ZonedDateTime;

@Configuration
public class ZonedDateConfiguration {

    @Bean
    ZonedDateTime startZonedDateTime() {
        return ZonedDateTime.now();
    }
}
