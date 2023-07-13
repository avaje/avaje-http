package io.avaje.http.generator.core;

public class Constants {

  /**
   * The suffix used by avaje-inject for generated factories.
   * A factory is generated automatically for request scoped controllers.
   */
  public static final String FACTORY_SUFFIX = "$Factory";

  static final String OPENAPIDEFINITION = "io.swagger.v3.oas.annotations.OpenAPIDefinition";
  static final String COMPONENT = "io.avaje.inject.Component";

  static final String SINGLETON_JAKARTA = "jakarta.inject.Singleton";
  static final String SINGLETON_JAVAX = "javax.inject.Singleton";

  static final String IMPORT_PATH_TYPE_CONVERT = "import static io.avaje.http.api.PathTypeConversion.*;";

  static final String IMPORT_HTTP_API = "io.avaje.http.api.*";
  static final String VALIDATOR = "io.avaje.http.api.Validator";
  public static final String META_INF_COMPONENT = "META-INF/services/io.avaje.http.client.HttpClient$GeneratedComponent";
  public static final String ACCEPT_LANGUAGE = "Accept-Language";


}
