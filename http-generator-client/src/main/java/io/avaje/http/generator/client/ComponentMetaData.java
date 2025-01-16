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

  void add(String type) {
    generatedClients.add(type);
  }

  void setFullName(String fullName) {
    this.fullName = fullName;
  }

  String fullName() {
    if (fullName == null) {
      String topPackage = TopPackage.of(generatedClients);
      fullName = topPackage + ".GeneratedHttpComponent";
    }
    return fullName;
  }

  List<String> all() {
    return generatedClients;
  }

  /** Return the package imports for the JsonAdapters and related types. */
  Collection<String> allImports() {
    final Set<String> packageImports = new TreeSet<>(generatedClients);
    generatedClients.stream()
      .map(ClientSuffix::toInterface)
      .forEach(packageImports::add);

    return packageImports;
  }
}
