package io.avaje.http.generator.core;

import javax.lang.model.type.TypeMirror;
import java.util.*;

public interface UType {

  /**
   * Create the UType from the given TypeMirror.
   */
  static UType parse(TypeMirror type) {
    return Util.parseType(type);
  }

  /**
   * Create the UType from the given String.
   */
  static UType parse(String type) {
    return Util.parse(type);
  }

  UType VOID = new VoidType();

  /**
   * Return the import types.
   */
  Set<String> importTypes();

  /**
   * Return the short name.
   */
  String shortType();

  /**
   * Return the short name.
   */
  String shortName();

  /**
   * Return the main type (outer most type).
   */
  String mainType();

  /**
   * Return the first generic parameter.
   */
  default String param0() {
    return null;
  }

  /**
   * Return the second generic parameter.
   */
  default String param1() {
    return null;
  }

  /**
   * Return the raw generic parameter if this UType is a Collection.
   */
  default UType paramRaw() {
    return null;
  }

  /**
   * Return the raw type.
   */
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

  /**
   * Simple non-generic type.
   */
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
      return rawType.startsWith("java.lang.") && rawType.indexOf('.') > -1
        ? Set.of()
        : Collections.singleton(rawType.replace("[]", ""));
    }

    @Override
    public String shortType() {
      return Util.shortName(rawType);
    }

    @Override
    public String shortName() {
      return Util.initLower(shortType()).replace(".", "$");
    }

    @Override
    public String mainType() {
      return rawType;
    }
  }

  /**
   * Generic type.
   */
  class Generic implements UType {
    final String rawType;
    final UType rawParamType;
    final List<String> allTypes;
    final String shortRawType;
    final String shortName;

    Generic(String rawTypeInput) {
      this.rawType = rawTypeInput.replace(" ", ""); // trim whitespace
      this.allTypes = Arrays.asList(rawType.split("[<|>|,]"));
      this.shortRawType = shortRawType(rawType, allTypes);
      this.shortName = Util.name(shortRawType);
      final var paramTypeString = extractRawParam();
      this.rawParamType = paramTypeString != null ? UType.parse(paramTypeString) : null;
    }

    private String extractRawParam() {
      switch (mainType()) {
        case "java.util.Set":
        case "java.util.List":
        case "java.util.stream.Stream":
        case "java.net.http.HttpResponse":
        case "java.util.concurrent.CompletableFuture":
        case "io.avaje.http.client.HttpCall":
          var first = rawType.indexOf("<") + 1;
          var end = rawType.lastIndexOf(">");
          return rawType.substring(first, end);
        case "java.util.Map":
          first = rawType.indexOf(",") + 1;
          end = rawType.lastIndexOf(">");
          return rawType.substring(first, end);
        default:
          return null;
      }
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
          if (type.startsWith("java")) {
            set.add(type.replace("[]", "").replace("?extends", ""));
          } else {
            set.add(innerTypesImport(type).replace("[]", "").replace("?extends", ""));
          }
        }
      }
      set.remove("?");
      return set;
    }

    public String innerTypesImport(String type) {
      final var parts = type.split("\\.");
      var result = "";
      var foundUpper = false;

      for (var i = 0; i < parts.length; i++) {
        if (!Character.isUpperCase(parts[i].charAt(0))) {
          result += parts[i] + ".";
        } else if (!foundUpper) {
          foundUpper = true;
          result += parts[i] + (i == parts.length - 1 ? "" : ".");
        } else {
          break;
        }
      }

      if (result.endsWith(".")) {
        result = result.substring(0, result.length() - 1);
      }
      return result;
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
      return shortName.replace(".", "$");
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

    @Override
    public UType paramRaw() {
      return rawParamType;
    }
  }
}
