package com.magicmarvel.spring.aop.after;

import org.junit.Test;
import org.magicmarvel.spring.context.context.AnnotationConfigApplicationContext;
import org.magicmarvel.spring.context.io.property.PropertyResolver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class AfterProxyTest {

    @Test
    public void testAfterProxy() throws URISyntaxException, IOException {
        try (var ctx = new AnnotationConfigApplicationContext(AfterApplication.class, createPropertyResolver())) {
            GreetingBean proxy = ctx.getBean(GreetingBean.class);
            // should change return value:
            assertEquals("Hello, Bob!", proxy.hello("Bob"));
            assertEquals("Morning, Alice!", proxy.morning("Alice"));
        }
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        return new PropertyResolver(ps);
    }
}
