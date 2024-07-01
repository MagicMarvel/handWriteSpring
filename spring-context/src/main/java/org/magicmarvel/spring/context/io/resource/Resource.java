package org.magicmarvel.spring.context.io.resource;

/**
 * 资源类
 *
 * @param path 这个资源在机器上的具体路径
 * @param name 这个资源的包名，用/分割 如com/magicmarvel/context/io/resource/Resource.class
 */
public record Resource(String path, String name) {
}
