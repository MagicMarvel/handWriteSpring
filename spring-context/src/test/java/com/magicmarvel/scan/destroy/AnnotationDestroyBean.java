package com.magicmarvel.scan.destroy;

import jakarta.annotation.PreDestroy;
import org.magicmarvel.handWriteSpring.annotation.Component;
import org.magicmarvel.handWriteSpring.annotation.Value;

@Component
public class AnnotationDestroyBean {

    @Value("${app.title}")
    public String appTitle;

    @PreDestroy
    void destroy() {
        this.appTitle = null;
    }
}
