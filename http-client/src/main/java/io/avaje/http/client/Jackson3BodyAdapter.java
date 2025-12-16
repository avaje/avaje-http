package io.avaje.http.client;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.type.CollectionType;

/**
 * Jackson 3.x BodyAdapter to read and write beans as JSON.
 *
 * <pre>{@code
 * HttpClient.builder()
 *     .baseUrl(baseUrl)
 *     .bodyAdapter(new Jackson3BodyAdapter())
 *     .build();
 *
 * }</pre>
 */
public final class Jackson3BodyAdapter implements BodyAdapter {

  private final ObjectMapper mapper;

  private final ConcurrentHashMap<Type, BodyWriter<?>> beanWriterCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Type, BodyReader<?>> beanReaderCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Type, BodyReader<?>> listReaderCache = new ConcurrentHashMap<>();

  /** Create passing the ObjectMapper to use. */
  public Jackson3BodyAdapter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  /** Create with a ObjectMapper that allows unknown properties and inclusion non empty. */
  public Jackson3BodyAdapter() {
    this.mapper = new ObjectMapper();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyWriter<T> beanWriter(Class<?> cls) {
    return (BodyWriter<T>) beanWriterCache.computeIfAbsent(cls, aClass -> {
      try {
        return new JWriter<>(mapper.writerFor(cls));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<T> beanReader(Class<T> cls) {
    return (BodyReader<T>) beanReaderCache.computeIfAbsent(cls, aClass -> {
      try {
        return new JReader<>(mapper.readerFor(cls));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyWriter<T> beanWriter(Type cls) {
    return (BodyWriter<T>) beanWriterCache.computeIfAbsent(cls, aClass -> {
      try {
        return new JWriter<>(mapper.writerFor(mapper.getTypeFactory().constructType(cls)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<T> beanReader(Type cls) {
    return (BodyReader<T>) beanReaderCache.computeIfAbsent(cls, aClass -> {
      try {
        return new JReader<>(mapper.readerFor(mapper.getTypeFactory().constructType(cls)));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<List<T>> listReader(Class<T> cls) {
    return (BodyReader<List<T>>) listReaderCache.computeIfAbsent(cls, aClass -> {
      try {
        final CollectionType collectionType =
          mapper.getTypeFactory().constructCollectionType(List.class, cls);
        final ObjectReader reader = mapper.readerFor(collectionType);
        return new JReader<>(reader);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> BodyReader<List<T>> listReader(Type type) {
    return (BodyReader<List<T>>) listReaderCache.computeIfAbsent(type, aType -> {
      try {
        var javaType = mapper.getTypeFactory().constructType(aType);
        final CollectionType collectionType =
          mapper.getTypeFactory().constructCollectionType(List.class, javaType);
        final ObjectReader reader = mapper.readerFor(collectionType);
        return new JReader<>(reader);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private static final class JReader<T> implements BodyReader<T> {

    private final ObjectReader reader;

    JReader(ObjectReader reader) {
      this.reader = reader;
    }

    @Override
    public T readBody(String content) {
      return reader.readValue(content);
    }

    @Override
    public T read(BodyContent bodyContent) {
      return reader.readValue(bodyContent.content());
    }
  }

  private static final class JWriter<T> implements BodyWriter<T> {

    private final ObjectWriter writer;

    JWriter(ObjectWriter writer) {
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
      return BodyContent.asJson(writer.writeValueAsBytes(bean));
    }
  }
}
