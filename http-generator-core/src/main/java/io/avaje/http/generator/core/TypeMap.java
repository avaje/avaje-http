package io.avaje.http.generator.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map of supported types with conversions from url path and query parameters.
 * <p/>
 * These types convert from String to types on controller methods.
 */
class TypeMap {

  private static final Map<String, TypeHandler> types = new HashMap<>();

  private static void add(TypeHandler h) {
    types.put(h.importTypes().get(0), h);
  }

  static {
    types.put("int", new IntHandler());
    types.put("long", new PLongHandler());
    types.put("double", new PDoubleHandler());
    types.put("float", new PFloatHandler());
    types.put("boolean", new BoolHandler());

    types.put("java.lang.String", new StringHandler());
    types.put("java.lang.Integer", new IntegerHandler());
    types.put("java.lang.Long", new LongHandler());
    types.put("java.lang.Double", new DoubleHandler());
    types.put("java.lang.Float", new FloatHandler());
    types.put("java.lang.Boolean", new BooleanHandler());

    add(new UuidHandler());
    add(new BigDecimalHandler());
    add(new BigIntegerHandler());
    add(new LocalDateHandler());
    add(new LocalTimeHandler());
    add(new LocalDateTimeHandler());
    add(new InstantHandler());
    add(new OffsetDateTimeHandler());
  }

  static TypeHandler get(String type) {
    return types.get(type);
  }

  static TypeHandler collectionHandler(UType type, boolean isEnum) {
    final var handler = types.get(type.param0());

    if (!isEnum && handler == null) {
      return null;
    }

    return types.computeIfAbsent(
        type.full(),
        k ->
            new CollectionHandler(
                isEnum ? enumParamHandler(type.paramRaw()) : handler,
                type.mainType().startsWith("java.util.Set"),
                isEnum));
  }

  static TypeHandler enumParamHandler(UType type) {
    return new EnumHandler(type);
  }

  static class StringHandler extends JavaLangType {
    StringHandler() {
      super("String");
    }

    @Override
    public String asMethod() {
      return null;
    }

    @Override
    public String toMethod() {
      return null;
    }
  }

  static class IntegerHandler extends JavaLangType {
    IntegerHandler() {
      super("Integer");
    }

    @Override
    public String asMethod() {
      return "asInteger(";
    }

    @Override
    public String toMethod() {
      return "toInteger(";
    }
  }

  static class IntHandler extends Primitive {
    IntHandler() {
      super("Int");
    }
  }

  static class LongHandler extends JavaLangType {
    LongHandler() {
      super("Long");
    }

    @Override
    public String asMethod() {
      return "asLong(";
    }

    @Override
    public String toMethod() {
      return "toLong(";
    }
  }

  static class PLongHandler extends Primitive {
    PLongHandler() {
      super("Long");
    }
  }

  static class FloatHandler extends JavaLangType {
    FloatHandler() {
      super("Float");
    }

    @Override
    public String asMethod() {
      return "asFloat(";
    }

    @Override
    public String toMethod() {
      return "toFloat(";
    }
  }

  static class PFloatHandler extends Primitive {
    PFloatHandler() {
      super("Float");
    }
  }

  static class DoubleHandler extends JavaLangType {
    DoubleHandler() {
      super("Double");
    }

    @Override
    public String asMethod() {
      return "asDouble(";
    }

    @Override
    public String toMethod() {
      return "toDouble(";
    }
  }

  static class PDoubleHandler extends Primitive {
    PDoubleHandler() {
      super("Double");
    }
  }

  static class BooleanHandler extends JavaLangType {
    BooleanHandler() {
      super("Boolean");
    }

    @Override
    public String asMethod() {
      return "asBool(";
    }

    @Override
    public String toMethod() {
      return "toBoolean(";
    }
  }

  static class BoolHandler extends Primitive {
    BoolHandler() {
      super("asBool(", "boolean");
    }
  }

  abstract static class JavaLangType implements TypeHandler {

    final String shortName;

    JavaLangType(String shortName) {
      this.shortName = shortName;
    }

    @Override
    public boolean isPrimitive() {
      return false;
    }

    @Override
    public String shortName() {
      return shortName;
    }

    @Override
    public List<String> importTypes() {
      return List.of();
    }
  }

  abstract static class Primitive implements TypeHandler {

    private final String type;

    private final String asMethod;

    Primitive(String asType) {
      this.asMethod = "as" + asType + "(";
      this.type = asType.toLowerCase();
    }

    Primitive(String asMethod, String type) {
      this.asMethod = asMethod;
      this.type = type;
    }

    @Override
    public boolean isPrimitive() {
      return true;
    }

    @Override
    public String shortName() {
      return type;
    }

    @Override
    public String asMethod() {
      return asMethod;
    }

    @Override
    public String toMethod() {
      return asMethod;
    }

    @Override
    public List<String> importTypes() {
      return List.of();
    }
  }

  static class UuidHandler extends ObjectHandler {
    UuidHandler() {
      super("java.util.UUID", "UUID");
    }
  }

  static class BigDecimalHandler extends ObjectHandler {
    BigDecimalHandler() {
      super("java.math.BigDecimal", "BigDecimal");
    }
  }

  static class BigIntegerHandler extends ObjectHandler {
    BigIntegerHandler() {
      super("java.math.BigInteger", "BigInteger");
    }
  }

  static class LocalDateHandler extends ObjectHandler {
    LocalDateHandler() {
      super("java.time.LocalDate", "LocalDate");
    }
  }

  static class InstantHandler extends ObjectHandler {
    InstantHandler() {
      super("java.time.Instant", "Instant");
    }
  }

  static class OffsetDateTimeHandler extends ObjectHandler {
    OffsetDateTimeHandler() {
      super("java.time.OffsetDateTime", "OffsetDateTime");
    }
  }

  static class LocalTimeHandler extends ObjectHandler {
    LocalTimeHandler() {
      super("java.time.LocalTime", "LocalTime");
    }
  }

  static class LocalDateTimeHandler extends ObjectHandler {
    LocalDateTimeHandler() {
      super("java.time.LocalDateTime", "LocalDateTime");
    }
  }

  static class EnumHandler extends ObjectHandler {
    private final UType type;

    EnumHandler(UType type) {

      super(type.mainType(), type.shortName());
      this.type = type;
    }

    @Override
    public String toMethod() {
      return "(" + type.shortType() + ") asEnum(" + type.shortType() + ".class, ";
    }

    @Override
    public String asMethod() {
      return "(" + type.shortType() + ") asEnum(" + type.shortType() + ".class, ";
    }
  }

  static class CollectionHandler implements TypeHandler {

    private final List<String> importTypes;
    private final String shortName;
    private String toMethod;

    CollectionHandler(TypeHandler handler, boolean set, boolean isEnum) {

      this.importTypes = new ArrayList<>(handler.importTypes());
      importTypes.add("io.avaje.http.api.PathTypeConversion");
      this.shortName = handler.shortName();
      this.toMethod =
          (set ? "set" : "list")
              + "("
              + (isEnum
                  ? "qp -> " + handler.toMethod() + " qp)"
                  : "PathTypeConversion::as" + shortName)
              + ", ";

      this.toMethod = toMethod.replace("PathTypeConversion::asString", "Object::toString");
    }

    @Override
    public boolean isPrimitive() {
      return false;
    }

    @Override
    public List<String> importTypes() {

      return importTypes;
    }

    @Override
    public String shortName() {
      return shortName;
    }

    @Override
    public String asMethod() {
      return null;
    }

    @Override
    public String toMethod() {
      return toMethod;
    }
  }

  abstract static class ObjectHandler implements TypeHandler {

    private final String importType;
    private final String shortName;
    private final String asMethod;
    private final String toMethod;

    ObjectHandler(String importType, String shortName) {
      this.importType = importType;
      this.shortName = shortName;
      this.asMethod = "as" + shortName + "(";
      this.toMethod = "to" + shortName + "(";
    }

    @Override
    public boolean isPrimitive() {
      return false;
    }

    @Override
    public List<String> importTypes() {
      return List.of(importType);
    }

    @Override
    public String shortName() {
      return shortName;
    }

    @Override
    public String asMethod() {
      return asMethod;
    }

    @Override
    public String toMethod() {
      return toMethod;
    }
  }
}
