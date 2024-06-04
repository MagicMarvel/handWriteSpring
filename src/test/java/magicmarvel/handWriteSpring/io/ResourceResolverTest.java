package magicmarvel.handWriteSpring.io;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.sql.DataSourceDefinition;
import org.junit.Test;
import sub.AnnoScan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class ResourceResolverTest {

    @Test
    public void scanClass() throws URISyntaxException, IOException {
        var pkg = "magicmarvel.handWriteSpring.io";
        var rr = new ResourceResolver(pkg);
        List<String> classes = rr.scan(res -> {
            String name = res.name();
            if (name.endsWith(".class")) {
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });
        Collections.sort(classes);
        System.out.println(classes);
        String[] listClasses = new String[] {
                // list of some scan classes:
                "com.magicmarvel.scan.convert.ValueConverterBean", //
                "com.magicmarvel.scan.destroy.AnnotationDestroyBean", //
                "com.magicmarvel.scan.init.SpecifyInitConfiguration", //
                "com.magicmarvel.scan.proxy.OriginBean", //
                "com.magicmarvel.scan.proxy.FirstProxyBeanPostProcessor", //
                "com.magicmarvel.scan.proxy.SecondProxyBeanPostProcessor", //
                "com.magicmarvel.scan.nested.OuterBean", //
                "com.magicmarvel.scan.nested.OuterBean$NestedBean", //
                "com.magicmarvel.scan.sub1.Sub1Bean", //
                "com.magicmarvel.scan.sub1.sub2.Sub2Bean", //
                "com.magicmarvel.scan.sub1.sub2.sub3.Sub3Bean", //
        };
        for (String clazz : listClasses) {
//            assertTrue(classes.contains(clazz));
        }
    }

    @Test
    public void scanJar() throws URISyntaxException, IOException {
        var pkg = PostConstruct.class.getPackageName();
        var rr = new ResourceResolver(pkg);
        List<String> classes = rr.scan(res -> {
            String name = res.name();
            if (name.endsWith(".class")) {
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });
        // classes in jar:
        assertTrue(classes.contains(PostConstruct.class.getName()));
        assertTrue(classes.contains(PreDestroy.class.getName()));
        assertTrue(classes.contains(PermitAll.class.getName()));
        assertTrue(classes.contains(DataSourceDefinition.class.getName()));
        // jakarta.annotation.sub.AnnoScan is defined in classes:
        assertTrue(classes.contains(AnnoScan.class.getName()));
    }

    @Test
    public void scanTxt() throws URISyntaxException, IOException {
        var pkg = "com.magicmarvel.scan";
        var rr = new ResourceResolver(pkg);
        List<String> classes = rr.scan(res -> {
            String name = res.name();
            if (name.endsWith(".txt")) {
                return name.replace("\\", "/");
            }
            return null;
        });
        Collections.sort(classes);
        assertArrayEquals(new String[] {
                // txt files:
                "com/magicmarvel/scan/sub1/sub1.txt", //
                "com/magicmarvel/scan/sub1/sub2/sub2.txt", //
                "com/magicmarvel/scan/sub1/sub2/sub3/sub3.txt", //
        }, classes.toArray(String[]::new));
    }
}
