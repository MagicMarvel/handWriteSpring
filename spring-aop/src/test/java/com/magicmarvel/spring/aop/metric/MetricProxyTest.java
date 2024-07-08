package com.magicmarvel.spring.aop.metric;


import org.junit.Test;
import org.magicmarvel.spring.context.context.AnnotationConfigApplicationContext;
import org.magicmarvel.spring.context.context.BeanDefinition;
import org.magicmarvel.spring.context.io.property.PropertyResolver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.Assert.*;

public class MetricProxyTest {

    @Test
    public void testMetricProxy() throws URISyntaxException, IOException {
        try (var ctx = new AnnotationConfigApplicationContext(MetricApplication.class, createPropertyResolver())) {
            HashWorker worker = ctx.getBean(HashWorker.class);

            // proxy class, not origin class:
            assertNotSame(HashWorker.class, worker.getClass());

            String md5 = "0x5d41402abc4b2a76b9719d911017c592";
            String sha1 = "0xaaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d";
            String sha256 = "0x2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824";

            assertEquals(md5, worker.md5("hello"));
            assertEquals(sha1, worker.sha1("hello"));
            assertEquals(sha256, worker.sha256("hello"));

            // get metric time:
            MetricInvocationHandler metrics = ctx.getBean(MetricInvocationHandler.class);
            BeanDefinition beanDefinition = ctx.findBeanDefinition(MetricInvocationHandler.class);
            assertEquals(Long.valueOf(5), metrics.lastProcessedTime.get("MD5"));
            assertEquals(Long.valueOf(256), metrics.lastProcessedTime.get("SHA-256"));
            // cannot metric sha1() because it is a final method:
            assertNull(metrics.lastProcessedTime.get("SHA-1"));
        }
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        return new PropertyResolver(ps);
    }
}
