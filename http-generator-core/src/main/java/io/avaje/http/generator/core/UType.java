package io.avaje.http.generator.core;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface UType {

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
   * Return the first generic parameter.
   */
  String param0();

  /**
   * Return the raw type.
   */
  String full();

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
    public String param0() {
      return null;
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
      return Collections.singleton(rawType);
    }

    @Override
    public String shortType() {
      return Util.shortName(rawType);
    }

    @Override
    public String param0() {
      return null;
    }
  }

  /**
   * Generic type.
   */
  class Generic implements UType {
    final String rawType;
    final String mainType;
    final List<String> params;
    Generic(String rawType, String mainType, List<String> params) {
      this.rawType = rawType;
      this.mainType = mainType;
      this.params = params;
    }

    @Override
    public String full() {
      return rawType;
    }

    @Override
    public Set<String> importTypes() {
      Set<String> set = new LinkedHashSet<>();
      set.add(mainType);
      set.addAll(params);
      return set;
    }

    @Override
    public String shortType() {
      StringBuilder sb = new StringBuilder();
      sb.append(Util.shortName(mainType)).append("<");
      for (int i = 0; i < params.size(); i++) {
        if (i > 0) {
          sb.append(",");
        }
        sb.append(Util.shortName(params.get(i)));
      }
      return sb.append(">").toString();
    }

    @Override
    public String param0() {
      return params.get(0);
    }
  }
}
