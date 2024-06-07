package com.magicmarvel.scan.init;

import com.magicmarvel.handWriteSpring.annotation.Component;
import com.magicmarvel.handWriteSpring.annotation.Value;
import jakarta.annotation.PostConstruct;

@Component
public class AnnotationInitBean {

    public String appName;
    @Value("${app.title}")
    String appTitle;
    @Value("${app.version}")
    String appVersion;

    @PostConstruct
    void init() {
        this.appName = this.appTitle + " / " + this.appVersion;
    }
}
