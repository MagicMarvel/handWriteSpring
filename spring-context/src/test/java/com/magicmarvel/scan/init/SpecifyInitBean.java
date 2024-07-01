package com.magicmarvel.scan.init;

public class SpecifyInitBean {

    final String appTitle;

    final String appVersion;

    public String appName;

    SpecifyInitBean(String appTitle, String appVersion) {
        this.appTitle = appTitle;
        this.appVersion = appVersion;
    }

    void init() {
        this.appName = this.appTitle + " / " + this.appVersion;
    }
}
