package io.avaje.http.generator.client;

import java.util.*;

import io.avaje.http.generator.core.APContext;


final class ComponentMetaData {

  private final Set<String> generatedClients = new HashSet<>();
  private String fullName;

  @Override
  public String toString() {
    return generatedClients.toString();
  }

  void add(String type) {
    generatedClients.add(type);
  }

  void setFullName(String fullName) {
    this.fullName = fullName;
  }

  String fullName() {
    if (fullName == null) {
      String topPackage = TopPackage.of(generatedClients);

      var defaultPackage =
        !topPackage.contains(".")
          && APContext.getProjectModuleElement().isUnnamed()
          && APContext.elements().getPackageElement(topPackage) == null;

      fullName =
        defaultPackage
          ? name(topPackage) + "HttpComponent"
          : topPackage + "." + name(topPackage) + "HttpComponent";
    }
    return fullName;
  }

  List<String> all() {
    return new ArrayList<>(generatedClients);
  }

  /** Return the package imports for the JsonAdapters and related types. */
  Collection<String> allImports() {
    final Set<String> packageImports = new TreeSet<>(generatedClients);
    generatedClients.stream()
      .map(ClientSuffix::toInterface)
      .forEach(packageImports::add);

    return packageImports;
  }


  static String name(String name) {
    if (name == null) {
      return null;
    }
    final int pos = name.lastIndexOf('.');
    if (pos > -1) {
      name = name.substring(pos + 1);
    }
    return camelCase(name).replaceFirst("Httpclient", "Generated");
  }

  private static String camelCase(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    boolean upper = true;
    for (char aChar : name.toCharArray()) {
      if (Character.isLetterOrDigit(aChar)) {
        if (upper) {
          aChar = Character.toUpperCase(aChar);
          upper = false;
        }
        sb.append(aChar);
      } else if (toUpperOn(aChar)) {
        upper = true;
      }
    }
    return sb.toString();
  }

  private static boolean toUpperOn(char aChar) {
    return aChar == ' ' || aChar == '-' || aChar == '_';
  }
}
