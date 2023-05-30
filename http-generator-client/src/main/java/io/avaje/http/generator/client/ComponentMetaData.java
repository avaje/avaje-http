package io.avaje.http.generator.client;

import java.util.*;

final class ComponentMetaData {

  private final List<String> generatedClients = new ArrayList<>();
  private String fullName;

  @Override
  public String toString() {
    return generatedClients.toString();
  }

  /** Ensure the component name has been initialised. */
  void initialiseFullName() {
    fullName();
  }

  boolean contains(String type) {
    return generatedClients.contains(type);
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
      if (!topPackage.endsWith(".httpclient")) {
        topPackage += ".httpclient";
      }
      fullName = topPackage + ".GeneratedHttpComponent";
    }
    return fullName;
  }

  String packageName() {
    return TopPackage.packageOf(fullName());
  }

  List<String> all() {
    return generatedClients;
  }

  /** Return the package imports for the JsonAdapters and related types. */
  Collection<String> allImports() {
    final Set<String> packageImports = new TreeSet<>(generatedClients);
    generatedClients.stream()
        .map(s -> removeLast(removeLast(s, ".httpclient"), "HttpClient"))
        .forEach(packageImports::add);

    return packageImports;
  }

  public static String removeLast(String className, String search) {
    final int pos = className.lastIndexOf(search);
    if (pos > -1) {
      return className.substring(0, pos) + className.substring(pos + search.length());
    }
    return className;
  }
}
