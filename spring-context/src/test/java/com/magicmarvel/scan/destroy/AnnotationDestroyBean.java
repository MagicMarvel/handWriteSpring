package com.magicmarvel.scan.destroy;

import jakarta.annotation.PreDestroy;
import org.magicmarvel.spring.context.annotation.Component;
import org.magicmarvel.spring.context.annotation.Value;

@Component
public class AnnotationDestroyBean {

    @Value("${app.title}")
    public String appTitle;

    @PreDestroy
    void destroy() {
        this.appTitle = null;
    }
}
