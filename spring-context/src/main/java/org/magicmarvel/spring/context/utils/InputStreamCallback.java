package org.magicmarvel.spring.context.utils;

import java.io.IOException;
import java.io.InputStream;


@FunctionalInterface
public interface InputStreamCallback<T> {

    T doWithInputStream(InputStream stream) throws IOException;
}
