package io.dinject.javalin.generator;

class Constants {

  static final String JAVALIN_CONTEXT = "io.javalin.Context";

  static final String JAVALIN_ROLES = "io.javalin.security.SecurityUtil.roles";

  static final String SINGLETON = "javax.inject.Singleton";
  static final String GENERATED = "javax.annotation.Generated";
  static final String API_BUILDER = "io.javalin.apibuilder.ApiBuilder";

  static final String AT_GENERATED = "@Generated(\"io.dinject.javalin.generator\")";

  static final String IMPORT_PATH_TYPE_CONVERT = "import static io.dinject.controller.PathTypeConversion.*;";

  static final String IMPORT_CONTROLLER = "io.dinject.controller.*";

}
