package io.dinject.javalin.generator;

class Constants {

  static final String JAVALIN2_CONTEXT = "io.javalin.Context";
  static final String JAVALIN3_CONTEXT = "io.javalin.http.Context";

  static final String JAVALIN2_ROLES = "io.javalin.security.SecurityUtil.roles";
  static final String JAVALIN3_ROLES = "io.javalin.core.security.SecurityUtil.roles";

  static final String OPENAPIDEFINITION = "io.swagger.v3.oas.annotations.OpenAPIDefinition";

  static final String SINGLETON = "javax.inject.Singleton";
  static final String GENERATED = "javax.annotation.Generated";
  static final String API_BUILDER = "io.javalin.apibuilder.ApiBuilder";

  static final String AT_GENERATED = "@Generated(\"io.dinject.javalin.generator\")";

  static final String IMPORT_PATH_TYPE_CONVERT = "import static io.dinject.controller.PathTypeConversion.*;";

  static final String IMPORT_CONTROLLER = "io.dinject.controller.*";
  static final String VALIDATOR = "io.dinject.controller.Validator";

}
