package io.avaje.http.client;

public interface BodyReader<T> {

  T read(BodyContent content);

}
