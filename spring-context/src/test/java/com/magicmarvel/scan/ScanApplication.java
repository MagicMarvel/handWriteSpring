package com.magicmarvel.scan;


import com.magicmarvel.imported.LocalDateConfiguration;
import com.magicmarvel.imported.ZonedDateConfiguration;
import org.magicmarvel.handWriteSpring.annotation.ComponentScan;
import org.magicmarvel.handWriteSpring.annotation.Import;

@ComponentScan
@Import({LocalDateConfiguration.class, ZonedDateConfiguration.class})
public class ScanApplication {

}
