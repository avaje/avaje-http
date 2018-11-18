package io.kanuka.web.javlin;

import java.util.HashMap;
import java.util.Map;

class TypeMap {

  private static final Map<String, TypeHandler> types = new HashMap<>();

  static {
    types.put("java.lang.String", new StringHander());
    types.put("int", new IntHander());
    types.put("long", new LongHander());
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
      this.asMethod = "as"+ asType + "(";
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
    public String getImportType() {
      return null;
    }
  }
}
