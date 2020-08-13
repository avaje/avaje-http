package io.avaje.http.client;

import java.util.List;

public interface BodyAdapter {

  BodyWriter beanWriter(Class<?> cls);

  <T> BodyReader<T> beanReader(Class<T> cls);

  <T> BodyReader<List<T>> listReader(Class<T> cls);

}
