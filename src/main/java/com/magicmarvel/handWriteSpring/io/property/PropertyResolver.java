package com.magicmarvel.handWriteSpring.io.property;

import jakarta.annotation.Nullable;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

public class PropertyResolver {

    // 存储所有的属性
    Map<String, String> properties = new HashMap<>();
    final Map<Class<?>, Function<String, Object>> converters = new HashMap<>();

    public PropertyResolver(Properties props) {
        // 存入环境变量:
        this.properties.putAll(System.getenv());
        // 存入Properties:
        Set<String> names = props.stringPropertyNames();
        for (String name : names) {
            this.properties.put(name, props.getProperty(name));
        }

        // 注册转换器
        converters.put(String.class, s -> s);
        converters.put(boolean.class, Boolean::parseBoolean);
        converters.put(Boolean.class, Boolean::valueOf);

        converters.put(byte.class, Byte::parseByte);
        converters.put(Byte.class, Byte::valueOf);

        converters.put(short.class, Short::parseShort);
        converters.put(Short.class, Short::valueOf);

        converters.put(int.class, Integer::parseInt);
        converters.put(Integer.class, Integer::valueOf);

        converters.put(long.class, Long::parseLong);
        converters.put(Long.class, Long::valueOf);

        converters.put(float.class, Float::parseFloat);
        converters.put(Float.class, Float::valueOf);

        converters.put(double.class, Double::parseDouble);
        converters.put(Double.class, Double::valueOf);

        converters.put(LocalDate.class, LocalDate::parse);
        converters.put(LocalTime.class, LocalTime::parse);
        converters.put(LocalDateTime.class, LocalDateTime::parse);
        converters.put(ZonedDateTime.class, ZonedDateTime::parse);
        converters.put(Duration.class, Duration::parse);
        converters.put(ZoneId.class, ZoneId::of);
    }


    /**
     * 根据key获取属性值，key有可能是一个${abc.xyz:defaultValue}这样的表达式，只会返回String类型的值，要返回其它指定的类型的值，需要使用另一个API
     * getProperty(String key, Class<T> type)
     *
     * @param key key有可能是一个${abc.xyz:defaultValue}这样的表达式
     * @return key对应的属性值，只会返回String类型的值
     */
    @Nullable
    public String getProperty(String key) {
        // 解析${abc.xyz:defaultValue}:
        PropertyExpr keyExpr = parsePropertyExpr(key);
        if (keyExpr != null) {
            if (keyExpr.defaultValue() != null) {
                // 带默认值查询:
                return getProperty(keyExpr.key(), getProperty(keyExpr.defaultValue()));
            } else {
                // 不带默认值查询:
                return getProperty(keyExpr.key());
            }
        }
        // 普通key查询:
        String value = this.properties.get(key);
        if (value != null) {
            // 用以支持这种查询${app.title:${APP_NAME:Summer}}
            // 在app.title查不到的时候，递归查APP_NAME
            return parseValue(value);
        }
        return null;
    }

    /**
     * 负责解决一个属性value也是${}查询的问题，如果这个value是一个${}查询，则递归再去查这个表达式的value
     * 如果不是${}查询，则直接返回这个value
     *
     * @param value 一个属性查出来的value，可能是一个${}查询，解析它
     * @return 解析后的value
     */
    String parseValue(String value) {
        PropertyExpr expr = parsePropertyExpr(value);
        if (expr == null) {
            return value;
        }
        // 是形如${abc.xyz:defaultValue}这样的表达式
        if (expr.defaultValue() != null) {
            return getProperty(expr.key(), expr.defaultValue());
        } else {
            return getProperty(expr.key());
        }
    }

    /**
     * 获取key对应的值，若key不存在，则返回defaultValue，这个key可以是一个${}表达式
     *
     * @param key          key
     * @param defaultValue 默认值
     * @return key对应的属性值
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    /**
     * 获取key对应的值，并转换为指定的类型
     *
     * @param key  key
     * @param type 类型
     * @param <T>  类型
     * @return key对应类型的值
     */
    public <T> T getProperty(String key, Class<T> type) {
        return convert(type, getProperty(key));
    }

    /**
     * 获取key对应的值，并转换为指定的类型，若key不存在，则返回defaultValue
     *
     * @param key          key
     * @param type         类型
     * @param defaultValue 默认值
     * @param <T>          类型
     * @return key对应类型的值
     */
    public <T> T getProperty(String key, Class<T> type, T defaultValue) {
        return convert(type, getProperty(key, defaultValue.toString()));
    }

    /**
     * 将形如${abc.xyz:defaultValue}这样的key解析成PropertyExpr
     *
     * @param key 形如${abc.xyz:defaultValue}这样的key
     * @return PropertyExpr，若不为${}表达式，则返回null
     */
    @Nullable
    PropertyExpr parsePropertyExpr(String key) {
        if (key.startsWith("${") && key.endsWith("}")) {
            // 是否存在defaultValue?
            int n = key.indexOf(':');
            if (n == -1) {
                // 没有defaultValue: ${key}
                String k = key.substring(2, key.length() - 1);
                return new PropertyExpr(k, null);
            } else {
                // 有defaultValue: ${key:default}
                String k = key.substring(2, n);
                return new PropertyExpr(k, key.substring(n + 1, key.length() - 1));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    <T> T convert(Class<T> clazz, String value) {
        Function<String, Object> fn = this.converters.get(clazz);
        if (fn == null) {
            throw new IllegalArgumentException("Unsupported value type: " + clazz.getName());
        }
        return (T) fn.apply(value);
    }

    public void getRequiredProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new NullPointerException("No such property: " + key);
        }
    }
}
