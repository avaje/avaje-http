package io.dinject.webroutegen;

import java.util.List;

/**
 * Adapter to specific platforms like Javalin and Helidon.
 */
public interface PlatformAdapter {

  /**
   * Return true if this type is the platform context type (Javalin Context etc).
   */
  boolean isContextType(String rawType);

  /**
   * Handle controller level roles.
   */
  void controllerRoles(List<String> roles, ControllerReader controller);

  /**
   * Handle method level roles.
   */
  void methodRoles(List<String> roles, ControllerReader controller);
}
