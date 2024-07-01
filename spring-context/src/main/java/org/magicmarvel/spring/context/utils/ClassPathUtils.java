package org.magicmarvel.spring.context.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;


public class ClassPathUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassPathUtils.class);

    /**
     * 读取指定路径的资源，并用callback处理它
     *
     * @param path                指定路径
     * @param inputStreamCallback 用以处理这个路径的函数
     * @param <T>                 处理结果的类型
     * @return 处理结果
     */
    public static <T> T readInputStream(String path, InputStreamCallback<T> inputStreamCallback) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        logger.atDebug().log("Read file from classpath: {}.", path);
        // TODO: 理解classLoader
        try (InputStream input = getContextClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new FileNotFoundException("File not found in classpath: " + path);
            }
            return inputStreamCallback.doWithInputStream(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readString(String path) {
        return readInputStream(path, (input) -> {
            byte[] data = input.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        });
    }

    private static ClassLoader getContextClassLoader() {
        ClassLoader cl;
        cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassPathUtils.class.getClassLoader();
        }
        return cl;
    }
}
