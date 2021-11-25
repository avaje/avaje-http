package io.avaje.http.client.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.avaje.http.client.BodyAdapter;
import io.avaje.http.client.BodyContent;
import io.avaje.http.client.BodyReader;
import io.avaje.http.client.BodyWriter;

import java.io.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Gson BodyAdapter.
 *
 * <pre>{@code
 *
 *   HttpClientContext.newBuilder()
 *       .withBaseUrl(baseUrl)
 *       .withRequestListener(new RequestLogger())
 *       //.withBodyAdapter(new JacksonBodyAdapter(new ObjectMapper()))
 *       .withBodyAdapter(new GsonBodyAdapter(new Gson()))
 *       .build();
 *
 * }</pre>
 */
public class GsonBodyAdapter implements BodyAdapter {

  private final Gson gson;

  private final ConcurrentHashMap<Class<?>, BodyWriter<?>> beanWriterCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Class<?>, BodyReader<?>> beanReaderCache = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Class<?>, BodyReader<?>> listReaderCache = new ConcurrentHashMap<>();

  /**
   * Create passing the Gson instance to use.
   */
  public GsonBodyAdapter(Gson gson) {
    this.gson = gson;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> BodyWriter<T> beanWriter(Class<?> cls) {
    return (BodyWriter<T>) beanWriterCache.computeIfAbsent(cls, aClass -> {
      try {
        final TypeAdapter adapter = gson.getAdapter(cls);
        return new Writer(gson, adapter);
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
        final TypeAdapter<T> adapter = gson.getAdapter(cls);
        return new Reader<>(gson, adapter);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> BodyReader<List<T>> listReader(Class<T> cls) {
    return (BodyReader<List<T>>) listReaderCache.computeIfAbsent(cls, aClass -> {
      try {
        final TypeToken listType = TypeToken.getParameterized(List.class, cls);
        final TypeAdapter<List<T>> adapter = gson.getAdapter(listType);
        return new Reader<>(gson, adapter);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private static class Reader<T> implements BodyReader<T> {

    private final Gson gson;
    private final TypeAdapter<T> adapter;

    Reader(Gson gson, TypeAdapter<T> adapter) {
      this.gson = gson;
      this.adapter = adapter;
    }

    /**
     * Read the content returning it as a java type.
     */
    @Override
    public T readBody(String content) {
      try {
        return adapter.fromJson(content);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public T read(BodyContent body) {
      try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(body.content()))) {
        final JsonReader jsonReader = gson.newJsonReader(reader);
        return adapter.read(jsonReader);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class Writer<T> implements BodyWriter<T> {

    private final Gson gson;
    private final TypeAdapter<T> adapter;

    Writer(Gson gson, TypeAdapter<T> adapter) {
      this.gson = gson;
      this.adapter = adapter;
    }

    @Override
    public BodyContent write(T bean, String contentType) {
      return write(bean);
    }

    @Override
    public BodyContent write(T bean) {
      try {
        ByteArrayOutputStream os = new ByteArrayOutputStream(200);
        JsonWriter jsonWriter = gson.newJsonWriter(new OutputStreamWriter(os, UTF_8));
        adapter.write(jsonWriter, bean);
        jsonWriter.close();
        return BodyContent.asJson(os.toByteArray());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
