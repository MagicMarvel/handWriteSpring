package com.magicmarvel.scan;


import com.magicmarvel.handWriteSpring.annotation.ComponentScan;
import com.magicmarvel.handWriteSpring.annotation.Import;
import com.magicmarvel.imported.LocalDateConfiguration;
import com.magicmarvel.imported.ZonedDateConfiguration;

@ComponentScan
@Import({LocalDateConfiguration.class, ZonedDateConfiguration.class})
public class ScanApplication {

}
