package com.magicmarvel.handWriteSpring.context;


import com.magicmarvel.handWriteSpring.io.property.PropertyResolver;
import com.magicmarvel.imported.LocalDateConfiguration;
import com.magicmarvel.imported.ZonedDateConfiguration;
import com.magicmarvel.scan.ScanApplication;
import com.magicmarvel.scan.custom.annotation.CustomAnnotationBean;
import com.magicmarvel.scan.nested.OuterBean;
import com.magicmarvel.scan.primary.DogBean;
import com.magicmarvel.scan.primary.PersonBean;
import com.magicmarvel.scan.primary.StudentBean;
import com.magicmarvel.scan.primary.TeacherBean;
import com.magicmarvel.scan.sub1.Sub1Bean;
import com.magicmarvel.scan.sub1.sub2.Sub2Bean;
import com.magicmarvel.scan.sub1.sub2.sub3.Sub3Bean;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class AnnotationConfigApplicationContextTest {

    @Test
    public void testAnnotationConfigApplicationContext() throws URISyntaxException, IOException {
        var ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        // @CustomAnnotation:
        assertNotNull(ctx.findBeanDefinition(CustomAnnotationBean.class));
        assertNotNull(ctx.findBeanDefinition("customAnnotation"));

        // @Import():
        assertNotNull(ctx.findBeanDefinition(LocalDateConfiguration.class));
        assertNotNull(ctx.findBeanDefinition("startLocalDate"));
        assertNotNull(ctx.findBeanDefinition("startLocalDateTime"));
        assertNotNull(ctx.findBeanDefinition(ZonedDateConfiguration.class));
        assertNotNull(ctx.findBeanDefinition("startZonedDateTime"));
        // nested:
        assertNotNull(ctx.findBeanDefinition(OuterBean.class));
        assertNotNull(ctx.findBeanDefinition(OuterBean.NestedBean.class));

        BeanDefinition studentDef = ctx.findBeanDefinition(StudentBean.class);
        BeanDefinition teacherDef = ctx.findBeanDefinition(TeacherBean.class);
        // 2 PersonBean:
        List<BeanDefinition> defs = ctx.findBeanDefinitions(PersonBean.class);
        assertSame(studentDef, defs.get(0));
        assertSame(teacherDef, defs.get(1));
        // 1 @Primary PersonBean:
        BeanDefinition personPrimaryDef = ctx.findBeanDefinition(PersonBean.class);
        assertSame(teacherDef, personPrimaryDef);
    }

    @Test
    public void testCustomAnnotation() throws URISyntaxException, IOException {
        var ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        assertNotNull(ctx.getBean(CustomAnnotationBean.class));
        assertNotNull(ctx.getBean("customAnnotation"));
    }

    @Test
    public void testImport() throws URISyntaxException, IOException {
        var ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        assertNotNull(ctx.getBean(LocalDateConfiguration.class));
        assertNotNull(ctx.getBean("startLocalDate"));
        assertNotNull(ctx.getBean("startLocalDateTime"));
        assertNotNull(ctx.getBean(ZonedDateConfiguration.class));
        assertNotNull(ctx.getBean("startZonedDateTime"));
    }

    @Test
    public void testNested() throws URISyntaxException, IOException {
        var ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        ctx.getBean(OuterBean.class);
        ctx.getBean(OuterBean.NestedBean.class);
    }

    @Test
    public void testPrimary() throws URISyntaxException, IOException {
        var ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        var person = ctx.getBean(PersonBean.class);
        assertEquals(TeacherBean.class, person.getClass());
        var dog = ctx.getBean(DogBean.class);
        assertEquals("Husky", dog.type);
    }

    @Test
    public void testSub() throws URISyntaxException, IOException {
        var ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        ctx.getBean(Sub1Bean.class);
        ctx.getBean(Sub2Bean.class);
        ctx.getBean(Sub3Bean.class);
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("app.title", "Scan App");
        ps.put("app.version", "v1.0");
        ps.put("jdbc.url", "jdbc:hsqldb:file:testdb.tmp");
        ps.put("jdbc.username", "sa");
        ps.put("jdbc.password", "");
        ps.put("convert.boolean", "true");
        ps.put("convert.byte", "123");
        ps.put("convert.short", "12345");
        ps.put("convert.integer", "1234567");
        ps.put("convert.long", "123456789000");
        ps.put("convert.float", "12345.6789");
        ps.put("convert.double", "123456789.87654321");
        ps.put("convert.localdate", "2023-03-29");
        ps.put("convert.localtime", "20:45:01");
        ps.put("convert.localdatetime", "2023-03-29T20:45:01");
        ps.put("convert.zoneddatetime", "2023-03-29T20:45:01+08:00[Asia/Shanghai]");
        ps.put("convert.duration", "P2DT3H4M");
        ps.put("convert.zoneid", "Asia/Shanghai");
        return new PropertyResolver(ps);
    }
}
