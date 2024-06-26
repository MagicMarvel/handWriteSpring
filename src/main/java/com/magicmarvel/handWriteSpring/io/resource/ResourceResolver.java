package com.magicmarvel.handWriteSpring.io.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(ResourceResolver.class);

    public ResourceResolver(String pkg) {
        this.basePackage = pkg;
    }

    public <T> List<T> scan(Function<Resource, T> mapper) throws URISyntaxException, IOException {
        List<T> reply = new ArrayList<>();

        ClassLoader contextClassLoader = getClassLoader();
        String basePackagePath = basePackage.replace(".", "/");
        // 这个getResource只会返回满足要求的文件夹，不会返回下面的文件，还需要自己遍历
        // 如这个输入 magicmarvel.handWriteSpring 会返回
        // file:/G:/summer-framework/step-by-step/handWriteSpring/target/test-classes/magicmarvel/handWriteSpring
        // file:/G:/summer-framework/step-by-step/handWriteSpring/target/classes/magicmarvel/handWriteSpring
        // 也有可能被传入一个jar包路径： jakarta.annotation
        // 会返回这样的东西： jar:file:/C:/Users/Keven/.m2/repository/jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar!/jakarta/annotation
        Enumeration<URL> resources = contextClassLoader.getResources(basePackagePath);
        while (resources.hasMoreElements()) {
            URI resourceURI = resources.nextElement().toURI();
            String uriStr = resourceURI.toString();
            String uriBaseStr = removeTrailingSlash(uriStr.substring(0, uriStr.length() - basePackagePath.length()));
            Path path;
            if (resourceURI.getScheme().equals("jar")) {
                // 如果basePackagePath是jar包路径
                String[] split = resourceURI.toString().split("!");
                FileSystem fileSystem = FileSystems.newFileSystem(URI.create(split[0]), Map.of());
                path = fileSystem.getPath(basePackagePath);
                findInPath(true, uriBaseStr, path, mapper, reply);
            } else {
                // 如果basePackagePath是文件路径
                path = Paths.get(resourceURI);
                findInPath(false, uriBaseStr, path, mapper, reply);
            }
        }
        return reply;
    }

    private <T> void findInPath(boolean isJar, String basePath, Path path, Function<Resource, T> mapper, List<T> reply) throws IOException {
        // try-with-resources 语句是一个 try 语句，它声明一个或多个资源。资源 是在程序完成后必须关闭的对象。try-with-resources 语句确保在语句结束时关闭每个资源。实现 java.lang.AutoCloseable 的任何对象（包括实现 java.io.Closeable 的所有对象）都可以用作资源。
        // 这个Files.walk会遍历给定文件夹下的所有文件夹和文件（包括被给定的文件夹，也会被遍历出来）
        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                Resource resource;
                if (isJar) {
                    resource = new Resource(basePath, file.toString());
                } else {
                    resource = new Resource("file:" + file.toString(),
                            file.toString().substring(basePath.length() - 5));
                }
                logger.atDebug().log("Find resource: {}", resource);
                T apply = mapper.apply(resource);
                if (apply != null) {
                    reply.add(apply);
                }
            });
        }
    }

    private ClassLoader getClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null) {
            // 目前我还不知道为啥要这样写
            contextClassLoader = getClass().getClassLoader();
        }
        return contextClassLoader;
    }

    // 移除字符串开头的斜杠
    private String removeLeadingSlash(String s) {
        if (s.startsWith("/") || s.startsWith("\\")) {
            s = s.substring(1);
        }
        return s;
    }

    // 移除字符串末尾的斜杠
    private String removeTrailingSlash(String s) {
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
