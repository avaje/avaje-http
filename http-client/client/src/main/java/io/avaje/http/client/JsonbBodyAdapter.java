package io.avaje.http.client;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * avaje jsonb BodyAdapter to read and write beans as JSON.
 *
 * <pre>{@code
 *
 *   HttpClientContext.builder()
 *       .baseUrl(baseUrl)
 *       .bodyAdapter(new JsonbBodyAdapter())
 *       .build();
 *
 * }</pre>
 */
public final class JsonbBodyAdapter implements BodyAdapter {

  private final Jsonb jsonb;
  private final ConcurrentHashMap<Class<?>, BodyWriter<?>> beanWriterCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Class<?>, BodyReader<?>> beanReaderCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Class<?>, BodyReader<?>> listReaderCache = new ConcurrentHashMap<>();

  /**
   * Create passing the Jsonb to use.
   */
  public JsonbBodyAdapter(Jsonb jsonb) {
    this.jsonb = jsonb;
  }

  /**
   * Create with a default Jsonb that allows unknown properties.
   */
  public JsonbBodyAdapter() {
    this.jsonb = Jsonb.newBuilder().build();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyWriter<T> beanWriter(Class<?> cls) {
    return (BodyWriter<T>) beanWriterCache.computeIfAbsent(cls, aClass -> new JWriter<>(jsonb.type(cls)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<T> beanReader(Class<T> cls) {
    return (BodyReader<T>) beanReaderCache.computeIfAbsent(cls, aClass -> new JReader<>(jsonb.type(cls)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<List<T>> listReader(Class<T> cls) {
    return (BodyReader<List<T>>) listReaderCache.computeIfAbsent(cls, aClass -> new JReader<>(jsonb.type(cls).list()));
  }

  private static class JReader<T> implements BodyReader<T> {

    private final JsonType<T> reader;

    JReader(JsonType<T> reader) {
      this.reader = reader;
    }

    @Override
    public T readBody(String content) {
      return reader.fromJson(content);
    }

    @Override
    public T read(BodyContent bodyContent) {
      return reader.fromJson(bodyContent.content());
    }
  }

  private static class JWriter<T> implements BodyWriter<T> {

    private final JsonType<T> writer;

    public JWriter(JsonType<T> writer) {
      this.writer = writer;
    }

    @Override
    public BodyContent write(T bean, String contentType) {
      // ignoring the requested contentType and always
      // writing the body as json content
      return write(bean);
    }

    @Override
    public BodyContent write(T bean) {
      return BodyContent.asJson(writer.toJsonBytes(bean));
    }
  }

}
