package io.avaje.http.generator.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TypeMapTest {

  @Test
  void get_int() {
    TypeHandler handler = TypeMap.get("int");
    assertThat(handler.asMethod()).isEqualTo("asInt(");
    assertTrue(handler.isPrimitive());
  }

  @Test
  void get_Integer() {
    TypeHandler handler = TypeMap.get("java.lang.Integer");
    assertThat(handler).isInstanceOf(TypeMap.IntegerHandler.class);
    assertThat(handler.asMethod()).isEqualTo("asInteger(");
    assertFalse(handler.isPrimitive());
  }

  @Test
  void get_double() {
    TypeHandler handler = TypeMap.get("double");
    assertThat(handler).isInstanceOf(TypeMap.PDoubleHandler.class);
    assertThat(handler.asMethod()).isEqualTo("asDouble(");
    assertTrue(handler.isPrimitive());
  }

  @Test
  void get_Double() {
    TypeHandler handler = TypeMap.get("java.lang.Double");
    assertThat(handler).isInstanceOf(TypeMap.DoubleHandler.class);
    assertThat(handler.asMethod()).isEqualTo("asDouble(");
    assertFalse(handler.isPrimitive());
  }

  @Test
  void get_float() {
    TypeHandler handler = TypeMap.get("float");
    assertThat(handler).isInstanceOf(TypeMap.PFloatHandler.class);
    assertThat(handler.asMethod()).isEqualTo("asFloat(");
    assertTrue(handler.isPrimitive());
  }

  @Test
  void get_Float() {
    TypeHandler handler = TypeMap.get("java.lang.Float");
    assertThat(handler).isInstanceOf(TypeMap.FloatHandler.class);
    assertThat(handler.asMethod()).isEqualTo("asFloat(");
    assertFalse(handler.isPrimitive());
  }
}
