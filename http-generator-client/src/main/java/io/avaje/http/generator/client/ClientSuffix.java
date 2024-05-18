package io.avaje.http.generator.client;

/**
 * Handling client naming suffix.
 * <p>
 * The suffix chosen should avoid repeating "HttpClient" or "Client"
 * in the generated class name.
 */
final class ClientSuffix {

  private static final String SUB_PACKAGE = ".httpclient";

  /**
   * Return the suffix to use for the generated client given the
   * client interface name.
   */
  static String fromInterface(String fullName) {
    if (fullName.endsWith("Client")) {
      return "Impl";
    } else {
      return "HttpClient";
    }
  }

  /**
   * Remove the suffix from the generated name.
   */
  static String removeSuffix(String clientName) {
    if (clientName.endsWith("Impl")) {
      return trim(clientName, 4);
    } else if (clientName.endsWith("HttpClient")) {
      return trim(clientName, 10);
    } else {
      return clientName;
    }
  }

  /**
   * Return the interface name from the generated client name.
   */
  static String toInterface(String clientName) {
    return removeSubPackage(removeSuffix(clientName));
  }

  private static String trim(String name, int length) {
    return name.substring(0, name.length() - length);
  }

  private static String removeSubPackage(String className) {
    final int pos = className.lastIndexOf(SUB_PACKAGE);
    if (pos > -1) {
      return className.substring(0, pos) + className.substring(pos + SUB_PACKAGE.length());
    }
    return className;
  }
}
