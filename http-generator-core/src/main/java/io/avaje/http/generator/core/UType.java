package io.avaje.http.generator.core;

import java.util.*;
import javax.lang.model.type.TypeMirror;

public interface UType {

  /** Create the UType from the given TypeMirror. */
  static UType parse(TypeMirror type) {
    return Util.parseType(type);
  }

  UType VOID = new VoidType();

  /** Return the import types. */
  Set<String> importTypes();

  /** Return the short name. */
  String shortType();

  /** Return the short name. */
  String shortName();

  /** Return the main type (outer most type). */
  String mainType();

  /** Return the first generic parameter. */
  default String param0() {
    return null;
  }

  /** Return the second generic parameter. */
  default String param1() {
    return null;
  }

  /** Return the raw type. */
  String full();

  default boolean isGeneric() {
    return false;
  }

  default String genericParams() {
    return "";
  }

  class VoidType implements UType {

    @Override
    public Set<String> importTypes() {
      return Collections.emptySet();
    }

    @Override
    public String shortType() {
      return "void";
    }

    @Override
    public String shortName() {
      return "void";
    }

    @Override
    public String mainType() {
      return "java.lang.Void";
    }

    @Override
    public String full() {
      return "void";
    }
  }

  /** Simple non-generic type. */
  class Basic implements UType {
    final String rawType;

    Basic(String rawType) {
      this.rawType = rawType;
    }

    @Override
    public String full() {
      return rawType;
    }

    @Override
    public Set<String> importTypes() {
      return Collections.singleton(rawType);
    }

    @Override
    public String shortType() {
      return Util.shortName(rawType);
    }

    @Override
    public String shortName() {
      return Util.initLower(shortType());
    }

    @Override
    public String mainType() {
      return rawType;
    }
  }

  /** Generic type. */
  class Generic implements UType {
    final String rawType;
    final List<String> allTypes;
    final String shortRawType;
    final String shortName;

    Generic(String rawTypeInput) {
      this.rawType = rawTypeInput.replace(" ", ""); // trim whitespace
      this.allTypes = Arrays.asList(rawType.split("[<|>|,]"));
      this.shortRawType = shortRawType(rawType, allTypes);
      this.shortName = Util.name(shortRawType);
    }

    private String shortRawType(String rawType, List<String> allTypes) {
      Map<String, String> typeMap = new LinkedHashMap<>();
      for (String val : allTypes) {
        typeMap.put(val, Util.shortName(val));
      }
      String shortRaw = rawType;
      for (Map.Entry<String, String> entry : typeMap.entrySet()) {
        shortRaw = shortRaw.replace(entry.getKey(), entry.getValue());
      }
      return shortRaw;
    }

    @Override
    public String full() {
      return rawType;
    }

    @Override
    public Set<String> importTypes() {
      Set<String> set = new LinkedHashSet<>();
      for (String type : allTypes) {
        if (!type.startsWith("java.lang.") && type.indexOf('.') > -1) {
          set.add(type);
        }
      }
      return set;
    }

    @Override
    public boolean isGeneric() {
      return true;
    }

    @Override
    public String genericParams() {
      final StringJoiner joiner = new StringJoiner(",");
      for (String type : allTypes) {
        if (type.indexOf('.') == -1) {
          joiner.add(type);
        }
      }
      final String commaDelim = joiner.toString();
      return commaDelim.isEmpty() ? "" : "<" + commaDelim + "> ";
    }

    @Override
    public String shortType() {
      return shortRawType;
    }

    @Override
    public String shortName() {
      return shortName;
    }

    @Override
    public String mainType() {
      return allTypes.isEmpty() ? null : allTypes.get(0);
    }

    @Override
    public String param0() {
      return allTypes.size() < 2 ? null : allTypes.get(1);
    }

    @Override
    public String param1() {
      return allTypes.size() < 3 ? null : allTypes.get(2);
    }
  }
}
