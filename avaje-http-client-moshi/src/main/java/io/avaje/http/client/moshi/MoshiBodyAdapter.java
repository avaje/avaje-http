package io.avaje.http.client.moshi;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import io.avaje.http.client.BodyAdapter;
import io.avaje.http.client.BodyContent;
import io.avaje.http.client.BodyReader;
import io.avaje.http.client.BodyWriter;

/**
 * Moshi BodyAdapter to read and write beans as JSON.
 *
 * <pre>{@code
 * HttpClient.builder()
 *     .baseUrl(baseUrl)
 *     .bodyAdapter(new MoshiBodyAdapter())
 *     .build();
 *
 * }</pre>
 */
public final class MoshiBodyAdapter implements BodyAdapter {

  private final Moshi moshi;
  private final ConcurrentHashMap<Type, BodyWriter<?>> beanWriterCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Type, BodyReader<?>> beanReaderCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Type, BodyReader<?>> listReaderCache = new ConcurrentHashMap<>();

  /** Create passing the Moshi to use. */
  public MoshiBodyAdapter(Moshi moshi) {
    this.moshi = moshi;
  }

  /** Create with a default Moshi that allows unknown properties. */
  public MoshiBodyAdapter() {
    this(new Moshi.Builder().build());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyWriter<T> beanWriter(Class<?> cls) {
    return (BodyWriter<T>)
        beanWriterCache.computeIfAbsent(cls, aClass -> new JWriter<>(moshi.adapter(cls)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyWriter<T> beanWriter(Type type) {
    return (BodyWriter<T>)
        beanWriterCache.computeIfAbsent(type, aClass -> new JWriter<>(moshi.adapter(type)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<T> beanReader(Class<T> cls) {
    return (BodyReader<T>)
        beanReaderCache.computeIfAbsent(cls, aClass -> new JReader<>(moshi.adapter(cls)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<T> beanReader(Type type) {
    return (BodyReader<T>)
        beanReaderCache.computeIfAbsent(type, aClass -> new JReader<>(moshi.adapter(type)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<List<T>> listReader(Type type) {

    return (BodyReader<List<T>>)
        listReaderCache.computeIfAbsent(
            type,
            aClass -> new JReader<>(moshi.adapter(Types.newParameterizedType(List.class, type))));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<List<T>> listReader(Class<T> cls) {
    return (BodyReader<List<T>>)
        listReaderCache.computeIfAbsent(
            cls,
            aClass -> new JReader<>(moshi.adapter(Types.newParameterizedType(List.class, cls))));
  }

  private static class JReader<T> implements BodyReader<T> {

    private final JsonAdapter<T> reader;

    JReader(JsonAdapter<T> reader) {
      this.reader = reader;
    }

    @Override
    public T readBody(String content) {
      try {
        return reader.fromJson(content);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public T read(BodyContent bodyContent) {
      try {
        return reader.fromJson(bodyContent.contentAsUtf8());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class JWriter<T> implements BodyWriter<T> {

    private final JsonAdapter<T> writer;

    public JWriter(JsonAdapter<T> writer) {
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
      return BodyContent.of(writer.toJson(bean));
    }
  }
}
