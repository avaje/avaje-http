package io.avaje.http.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jackson BodyAdapter to read and write beans as JSON.
 *
 * <pre>{@code
 *
 *   HttpClientContext.newBuilder()
 *       .withBaseUrl(baseUrl)
 *       .withRequestListener(new RequestLogger())
 *       .withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
 *       //.withBodyAdapter(new GsonBodyAdapter(new Gson()))
 *       .build();
 *
 * }</pre>
 */
public class JacksonBodyAdapter implements BodyAdapter {

  private final ObjectMapper mapper;

  private final ConcurrentHashMap<Class<?>, BodyWriter> beanWriterCache = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<Class<?>, BodyReader<?>> beanReaderCache = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<Class<?>, BodyReader<?>> listReaderCache = new ConcurrentHashMap<>();

  public JacksonBodyAdapter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public BodyWriter beanWriter(Class<?> cls) {
    return beanWriterCache.computeIfAbsent(cls, aClass -> {
      try {
        return new JWriter(mapper.writerFor(cls));
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
  public <T> BodyReader<List<T>> listReader(Class<T> cls) {
    return (BodyReader<List<T>>) listReaderCache.computeIfAbsent(cls, aClass -> {
      try {
        final CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, cls);
        final ObjectReader reader = mapper.readerFor(collectionType);
        return new JReader<>(reader);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private static class JReader<T> implements BodyReader<T> {

    private final ObjectReader reader;

    JReader(ObjectReader reader) {
      this.reader = reader;
    }

    @Override
    public T read(BodyContent s) {
      try {
        return reader.readValue(s.content());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class JWriter implements BodyWriter {

    private final ObjectWriter writer;

    public JWriter(ObjectWriter writer) {
      this.writer = writer;
    }

    @Override
    public BodyContent write(Object bean, String contentType) {
      // ignoring the requested contentType and always
      // writing the body as json content
      return write(bean);
    }

    @Override
    public BodyContent write(Object bean) {
      try {
        return BodyContent.asJson(writer.writeValueAsBytes(bean));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
