package io.dinject.webroutegen;

import java.util.Set;

/**
 * Adapter to specific platforms like Javalin and Helidon.
 */
public interface PlatformAdapter {

  String rolesStaticImport();

  boolean isContextType(String rawType);

  String atGenerated();

  Set<String> controllerImports();
}
