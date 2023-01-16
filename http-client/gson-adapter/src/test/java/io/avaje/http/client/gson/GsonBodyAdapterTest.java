package io.avaje.http.client.gson;

import com.google.gson.Gson;
import io.avaje.http.client.BodyContent;
import io.avaje.http.client.BodyReader;
import io.avaje.http.client.BodyWriter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GsonBodyAdapterTest {

  private final GsonBodyAdapter adapter = new GsonBodyAdapter(new Gson());

  @Test
  void beanWriter() {

    final Foo foo = new Foo();
    foo.id = 42;
    foo.name = "bar";

    final BodyWriter writer = adapter.beanWriter(Foo.class);
    final BodyContent content = writer.write(foo);

    final String json = new String(content.content(), StandardCharsets.UTF_8);
    assertEquals("{\"id\":42,\"name\":\"bar\"}", json);
  }

  @Test
  void beanReader() {

    final BodyReader<Foo> reader = adapter.beanReader(Foo.class);

    final Foo read = reader.read(content("{\"id\":42, \"name\":\"bar\"}"));
    assertEquals(42, read.id);
    assertEquals("bar", read.name);
  }

  @Test
  void listReader() {

    final BodyReader<List<Foo>> reader = adapter.listReader(Foo.class);

    final List<Foo> read = reader.read(content("[{\"id\":42, \"name\":\"bar\"},{\"id\":43, \"name\":\"baz\"}]"));

    assertEquals(2, read.size());
    assertEquals(42, read.get(0).id);
    assertEquals(43, read.get(1).id);
  }

  BodyContent content(String raw) {
    return new BodyContent("not-used", raw.getBytes());
  }
}
