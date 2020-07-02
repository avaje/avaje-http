package io.dinject.webroutegen;

import java.util.HashSet;
import java.util.Set;

public class JavalinAdapter implements PlatformAdapter {

  static final String JAVALIN3_CONTEXT = "io.javalin.http.Context";
  static final String JAVALIN3_ROLES = "io.javalin.core.security.SecurityUtil.roles";
  static final String AT_GENERATED = "@Generated(\"io.dinject.javalin-webgen\")";
  static final String API_BUILDER = "io.javalin.apibuilder.ApiBuilder";

  private final Set<String> controllerImports = new HashSet<>();

  JavalinAdapter() {
    controllerImports.add(API_BUILDER);
  }

  @Override
  public Set<String> controllerImports() {
    return controllerImports;
  }

  @Override
  public String rolesStaticImport() {
    return JAVALIN3_ROLES;
  }

  @Override
  public boolean isContextType(String rawType) {
    return JAVALIN3_CONTEXT.equals(rawType);
  }

  @Override
  public String atGenerated() {
    return AT_GENERATED;
  }

}
