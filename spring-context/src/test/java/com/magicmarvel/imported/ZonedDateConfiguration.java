package com.magicmarvel.imported;


import org.magicmarvel.handWriteSpring.annotation.Bean;
import org.magicmarvel.handWriteSpring.annotation.Configuration;

import java.time.ZonedDateTime;

@Configuration
public class ZonedDateConfiguration {

    @Bean
    ZonedDateTime startZonedDateTime() {
        return ZonedDateTime.now();
    }
}
