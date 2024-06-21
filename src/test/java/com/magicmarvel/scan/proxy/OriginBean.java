package com.magicmarvel.scan.proxy;

import com.magicmarvel.handWriteSpring.annotation.Component;
import com.magicmarvel.handWriteSpring.annotation.Value;

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
