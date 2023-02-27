package io.avaje.http.generator.core;

import java.util.List;

/**
 * Adapter to specific platforms like Javalin and Helidon.
 */
public interface PlatformAdapter {

  /**
   * Return true if this type is the platform specific request, response or context type.
   * For example Javalin Context, Helidon ServerRequest or ServerResponse type).
   */
  boolean isContextType(String rawType);

  /**
   * Return the platform specific parameter (request, response or context).
   */
  String platformVariable(String rawType);

  /**
   * Return platform specific code to return the body content.
   */
  String bodyAsClass(UType type);

  /**
   * Return true if body is passed as a method parameter.
   */
  boolean isBodyMethodParam();

  /**
   * Return whitespace indent for setting parameter values.
   */
  String indent();

  /**
   * Handle controller level roles.
   */
  void controllerRoles(List<String> roles, ControllerReader controller);

  /**
   * Handle method level roles.
   */
  void methodRoles(List<String> roles, ControllerReader controller);

  void writeReadParameter(Append writer, ParamType paramType, String paramName);

  void writeReadParameter(Append writer, ParamType paramType, String paramName, String paramDefault);

  void writeReadCollectionParameter(Append writer, ParamType paramType, String paramName);

  void writeReadCollectionParameter(Append writer, ParamType paramType, String paramName, List<String> paramDefault);

}
