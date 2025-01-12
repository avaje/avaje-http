package io.avaje.http.generator.core;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class JsonBUtilTest {

  @Test
  void writeType() {
    UType uType = UType.parse("my.pack.Foo");
    var sw = new StringWriter();
    JsonBUtil.writeType(uType, new Append(sw));

    assertThat(sw.toString()).isEqualTo("Foo.class)");
  }

  @Test
  void writeType_generic() {
    UType uType = UType.parse("my.pack.Some<java.lang.String>");
    var sw = new StringWriter();
    JsonBUtil.writeType(uType, new Append(sw));

    assertThat(sw.toString()).isEqualTo("Types.newParameterizedType(Some.class, String.class))");
  }

  @Test
  void writeType_genericWithWildcard() {
    UType uType = UType.parse("my.pack.Some<java.lang.String, ?>");
    var sw = new StringWriter();
    JsonBUtil.writeType(uType, new Append(sw));

    assertThat(sw.toString()).isEqualTo("Types.newParameterizedType(Some.class, String.class, Object.class))");
  }

  @Test
  void writeType_genericWithMultiple() {
    UType uType = UType.parse("my.pack.Some<java.lang.String, my.other.Foo>");
    var sw = new StringWriter();
    JsonBUtil.writeType(uType, new Append(sw));

    assertThat(sw.toString()).isEqualTo("Types.newParameterizedType(Some.class, String.class, Foo.class))");
  }
}
