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
    UType uType = UType.parse("java.util.Optional<java.lang.Integer>");
    TypeHandler optionalHandler = TypeMap.optionalHandler(uType, false);
    assertThat(optionalHandler).isNotNull();
    assertThat(optionalHandler.toMethod()).isEqualTo("optional(PathTypeConversion::asInteger, ");
  }

  @Test
  void get_OptionalString() {
    UType uType = UType.parse("java.util.Optional<java.lang.String>");
    TypeHandler optionalHandler = TypeMap.optionalHandler(uType, false);
    assertThat(optionalHandler).isNotNull();
    assertThat(optionalHandler.toMethod()).isEqualTo("optional(");
  }

  @Test
  void get_OptionalEnum() {
    UType uType = UType.parse("java.util.Optional<org.my.MyEnum>");
    TypeHandler optionalHandler = TypeMap.optionalHandler(uType, true);
    assertThat(optionalHandler).isNotNull();
    assertThat(optionalHandler.toMethod()).isEqualTo("optional(qp -> (MyEnum) toEnum(MyEnum.class,  qp), ");
  }

  @Test
  void get_ListInteger() {
    UType uType = UType.parse("java.util.List<java.lang.Integer>");
    TypeHandler handler = TypeMap.collectionHandler(uType, false);
    assertThat(handler).isNotNull();
    assertThat(handler.toMethod()).isEqualTo("list(PathTypeConversion::asInteger, ");
  }

  @Test
  void get_SetInteger() {
    UType uType = UType.parse("java.util.Set<java.lang.Integer>");
    TypeHandler handler = TypeMap.collectionHandler(uType, false);
    assertThat(handler).isNotNull();
    assertThat(handler.toMethod()).isEqualTo("set(PathTypeConversion::asInteger, ");
  }

  @Test
  void get_ListString() {
    UType uType = UType.parse("java.util.List<java.lang.String>");
    TypeHandler handler = TypeMap.collectionHandler(uType, false);
    assertThat(handler).isNotNull();
    assertThat(handler.toMethod()).isEqualTo("list(Object::toString, ");
  }

  @Test
  void get_SetString() {
    UType uType = UType.parse("java.util.Set<java.lang.String>");
    TypeHandler handler = TypeMap.collectionHandler(uType, false);
    assertThat(handler).isNotNull();
    assertThat(handler.toMethod()).isEqualTo("set(Object::toString, ");
  }

  @Test
  void get_ListEnum() {
    UType uType = UType.parse("java.util.List<org.my.MyEnum>");
    TypeHandler handler = TypeMap.collectionHandler(uType, true);
    assertThat(handler).isNotNull();
    assertThat(handler.toMethod()).isEqualTo("list(qp -> (MyEnum) toEnum(MyEnum.class,  qp), ");
  }

  @Test
  void get_SetEnum() {
    UType uType = UType.parse("java.util.Set<org.my.MyEnum>");
    TypeHandler handler = TypeMap.collectionHandler(uType, true);
    assertThat(handler).isNotNull();
    assertThat(handler.toMethod()).isEqualTo("set(qp -> (MyEnum) toEnum(MyEnum.class,  qp), ");
  }
}
