package magicmarvel.handWriteSpring.io;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class ResourceResolver {
    private final String basePackage;

    public ResourceResolver(String pkg) {
        this.basePackage = pkg;
    }

    public <T> List<T> scan(Function<Resource, T> func) throws URISyntaxException, IOException {
        List<T> reply = new ArrayList<>();
        Stream<Path> paths = null;

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        String basePackagePath = basePackage.replace(".", "/");
        Enumeration<URL> resources = contextClassLoader.getResources(basePackagePath);
        while (resources.hasMoreElements()) {
            try {
                URL resourceURL = resources.nextElement();
                URI uri = resourceURL.toURI();
                Path path = null;
                if (uri.getScheme().equals("jar")) {
                    // 如果basePackagePath是jar包路径
                    String[] split = uri.toString().split("!");
                    FileSystem fileSystem = FileSystems.newFileSystem(URI.create(split[0]), Map.of());
                    path = fileSystem.getPath(basePackagePath);
                } else {
                    // 如果basePackagePath是文件路径
                    path = Paths.get(uri);
                }
                paths = Files.walk(path);
                paths.filter(Files::isRegularFile).forEach(file -> {
                    String name = file.getFileName().toString();
                    Resource resource = new Resource(file.toString(), name);
                    T apply = func.apply(resource);
                    if (apply != null) {
                        reply.add(apply);
                    }
                });
            } finally {
                if (paths != null) {
                    paths.close();
                }
            }
        }
        return reply;
    }

}
