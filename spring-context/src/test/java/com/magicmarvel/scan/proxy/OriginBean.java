package com.magicmarvel.scan.proxy;


import org.magicmarvel.handWriteSpring.annotation.Component;
import org.magicmarvel.handWriteSpring.annotation.Value;

@Component
public class OriginBean {

    @Value("${app.title}")
    public String name;

    public String version;

    public String getName() {
        return name;
    }

    public String getVersion() {
        return this.version;
    }

    @Value("${app.version}")
    public void setVersion(String version) {
        this.version = version;
    }
}
