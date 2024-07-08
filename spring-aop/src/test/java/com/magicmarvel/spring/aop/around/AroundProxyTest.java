package com.magicmarvel.spring.aop.around;

import org.junit.Test;
import org.magicmarvel.spring.context.context.AnnotationConfigApplicationContext;
import org.magicmarvel.spring.context.io.property.PropertyResolver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.junit.Assert.*;

public class AroundProxyTest {

    @Test
    public void testAroundProxy() {
        try (var ctx = new AnnotationConfigApplicationContext(AroundApplication.class, createPropertyResolver())) {
            OriginBean proxy = ctx.getBean(OriginBean.class);
            // OriginBean$ByteBuddy$8NoD1FcQ
            System.out.println(proxy.getClass().getName());

            // proxy class, not origin class:
            assertNotSame(OriginBean.class, proxy.getClass());
            // proxy.name not injected:
            assertNull(proxy.name);

            assertEquals("Hello, Bob!", proxy.hello());
            assertEquals("Morning, Bob.", proxy.morning());

            // test injected proxy:
            OtherBean other = ctx.getBean(OtherBean.class);
            assertSame(proxy, other.origin);
            assertEquals("Hello, Bob!", other.origin.hello());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("customer.name", "Bob");
        return new PropertyResolver(ps);
    }
}
