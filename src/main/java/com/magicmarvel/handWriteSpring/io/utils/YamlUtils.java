package com.magicmarvel.handWriteSpring.io.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse yaml by snakeyaml:
 * <p>
 * <a href="https://github.com/snakeyaml/snakeyaml">...</a>
 */
@SuppressWarnings("unused")
public class YamlUtils {

    /**
     * 读取YML文件为Map（可嵌套）
     *
     * @param path 文件路径
     * @return 可能有嵌套的map
     */
    private static Map<String, Object> loadYaml(String path) {
        var loaderOptions = new LoaderOptions();
        var dumperOptions = new DumperOptions();
        var representer = new Representer(dumperOptions);
        var resolver = new NoImplicitResolver();
        var yaml = new Yaml(new Constructor(loaderOptions), representer, dumperOptions, loaderOptions, resolver);
        return ClassPathUtils.readInputStream(path, yaml::load);
    }

    /**
     * 读取YML文件并拍平为Map
     *
     * @param path 文件路径
     * @return 拍平后的Map，value值虽然写的是Object，但是只可能是list和string两种
     */
    public static Map<String, Object> loadYamlAsPlainMap(String path) {
        Map<String, Object> data = loadYaml(path);
        Map<String, Object> plain = new LinkedHashMap<>();
        convertTo(data, "", plain);
        return plain;
    }

    /**
     * 将嵌套的Map转换为扁平的Map
     *
     * @param source 原始Map
     * @param prefix 前缀
     * @param plain  扁平Map
     */
    private static void convertTo(Map<String, Object> source, String prefix, Map<String, Object> plain) {
        for (String key : source.keySet()) {
            Object value = source.get(key);
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) value;
                convertTo(subMap, prefix + key + ".", plain);
            } else if (value instanceof List) {
                plain.put(prefix + key, value);
            } else {
                plain.put(prefix + key, value.toString());
            }
        }
    }
}

/**
 * 关闭自动的类型转换，让读取出来的值全是String，我们自己转换
 */
class NoImplicitResolver extends Resolver {

    public NoImplicitResolver() {
        super();
        super.yamlImplicitResolvers.clear();
    }
}
