package org.magicmarvel.spring.context.io.property;

/**
 * 表示属性的类
 * @param key 这个属性的key值
 * @param defaultValue 这个属性的默认值
 */
public record PropertyExpr(String key, String defaultValue) {
}
