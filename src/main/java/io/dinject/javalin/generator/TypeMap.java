package io.dinject.javalin.generator;

import java.util.HashMap;
import java.util.Map;

/**
 * Map of supported types with conversions from url path and query parameters.
 * <p/>
 * These types convert from String to types on controller methods.
 */
class TypeMap {

  private static final Map<String, TypeHandler> types = new HashMap<>();

  private static void add(TypeHandler h) {
    types.put(h.getImportType(), h);
  }

  static {
    types.put("int", new IntHander());
    types.put("long", new LongHander());
    types.put("java.lang.String", new StringHander());
    types.put("java.lang.Integer", new IntegerHander());
    add(new UuidHandler());
    add(new LocalDateHandler());
  }

  static TypeHandler get(String type) {
    return types.get(type);
  }

  static class StringHander extends JavaLangType {
    StringHander() {
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

  static class IntegerHander extends JavaLangType {
    IntegerHander() {
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

  static class IntHander extends Primitive {
    IntHander() {
      super("Int");
    }
  }

  static class LongHander extends Primitive {
    LongHander() {
      super("Long");
    }
  }

  static abstract class JavaLangType implements TypeHandler {

    final String shortName;

    JavaLangType(String shortName) {
      this.shortName = shortName;
    }

    @Override
    public String shortName() {
      return shortName;
    }

    @Override
    public String getImportType() {
      return null;
    }
  }

  static abstract class Primitive implements TypeHandler {

    private final String type;

    private final String asMethod;

    Primitive(String asType) {
      this.asMethod = "as" + asType + "(";
      this.type = asType.toLowerCase();
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
    public String getImportType() {
      return null;
    }
  }

  static class UuidHandler extends ObjectHander {
    UuidHandler() {
      super("java.util.UUID", "UUID");
    }
  }

  static class LocalDateHandler extends ObjectHander {
    LocalDateHandler() {
      super("java.time.LocalDate", "LocalDate");
    }
  }

  static abstract class ObjectHander implements TypeHandler {

    private final String importType;
    private final String shortName;
    private final String asMethod;
    private final String toMethod;

    ObjectHander(String importType, String shortName) {
      this.importType = importType;
      this.shortName = shortName;
      this.asMethod = "as" + shortName + "(";
      this.toMethod = "to" + shortName + "(";
    }

    @Override
    public String getImportType() {
      return importType;
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
