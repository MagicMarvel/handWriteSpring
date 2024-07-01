package com.magicmarvel.scan.init;

import jakarta.annotation.PostConstruct;
import org.magicmarvel.spring.context.annotation.Component;
import org.magicmarvel.spring.context.annotation.Value;

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
