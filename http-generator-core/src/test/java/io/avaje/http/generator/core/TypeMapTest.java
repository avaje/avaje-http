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

  @Test
  void get_BigDecimal() {
    TypeHandler handler = TypeMap.get("java.math.BigDecimal");
    assertThat(handler).isInstanceOf(TypeMap.BigDecimalHandler.class);
    assertThat(handler.asMethod()).isEqualTo("asBigDecimal(");
    assertFalse(handler.isPrimitive());
  }

  @Test
  void get_BigInt() {
    TypeHandler handler = TypeMap.get("java.math.BigInteger");
    assertThat(handler).isInstanceOf(TypeMap.BigIntegerHandler.class);
    assertThat(handler.asMethod()).isEqualTo("asBigInteger(");
    assertFalse(handler.isPrimitive());
  }

  @Test
  void get_OptionalInteger() {
    TypeHandler handler = TypeMap.get("java.lang.Integer");
    TypeMap.OptionalHandler optionalHandler = new TypeMap.OptionalHandler(handler, false);
    assertThat(optionalHandler.toMethod()).isEqualTo("optional(PathTypeConversion::asInteger, ");
  }

  @Test
  void get_OptionalString() {
    TypeHandler handler = TypeMap.get("java.lang.String");
    TypeMap.OptionalHandler optionalHandler = new TypeMap.OptionalHandler(handler, false);
    assertThat(optionalHandler.toMethod()).isEqualTo("optional(");
  }

  @Test
  void get_OptionalEnum() {
    TypeHandler handler = TypeMap.enumParamHandler(UType.parse("org.my.MyEnum"));
    TypeMap.OptionalHandler optionalHandler = new TypeMap.OptionalHandler(handler, true);
    assertThat(optionalHandler.toMethod()).isEqualTo("optional(qp -> (MyEnum) toEnum(MyEnum.class,  qp), ");
  }
}
