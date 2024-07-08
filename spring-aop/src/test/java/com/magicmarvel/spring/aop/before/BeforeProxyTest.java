package com.magicmarvel.spring.aop.before;

import org.junit.Test;
import org.magicmarvel.spring.context.context.AnnotationConfigApplicationContext;
import org.magicmarvel.spring.context.io.property.PropertyResolver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class BeforeProxyTest {

    @Test
    public void testBeforeProxy() throws URISyntaxException, IOException {
        try (var ctx = new AnnotationConfigApplicationContext(BeforeApplication.class, createPropertyResolver())) {
            BusinessBean proxy = ctx.getBean(BusinessBean.class);
            // should print log:
            assertEquals("Hello, Bob.", proxy.hello("Bob"));
            assertEquals("Morning, Alice.", proxy.morning("Alice"));
        }
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        return new PropertyResolver(ps);
    }
}
