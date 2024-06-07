package com.magicmarvel.scan.destroy;

import com.magicmarvel.handWriteSpring.annotation.Component;
import com.magicmarvel.handWriteSpring.annotation.Value;
import jakarta.annotation.PreDestroy;

@Component
public class AnnotationDestroyBean {

    @Value("${app.title}")
    public String appTitle;

    @PreDestroy
    void destroy() {
        this.appTitle = null;
    }
}
