package io.avaje.http.generator.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentMetaDataTest {

  @Test
  void name() {
    assertThat(ComponentMetaData.name(null)).isNull();
    assertThat(ComponentMetaData.name("org.foo")).isEqualTo("Foo");
    assertThat(ComponentMetaData.name("org.fooBar")).isEqualTo("FooBar");
    assertThat(ComponentMetaData.name("org.FooBar")).isEqualTo("FooBar");
    assertThat(ComponentMetaData.name("org.FooBarHttpclient")).isEqualTo("FooBarGenerated");
    assertThat(ComponentMetaData.name("org.FooBarHttpclientAgainHttpclient")).isEqualTo("FooBarGeneratedAgainHttpclient");
  }
}
