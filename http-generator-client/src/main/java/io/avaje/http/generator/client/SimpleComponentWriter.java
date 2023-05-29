package io.avaje.http.generator.client;

import static io.avaje.http.generator.core.ProcessingContext.createMetaInfWriter;
import static io.avaje.http.generator.core.ProcessingContext.createWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.Util;

final class SimpleComponentWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-client-generator\")";

  private final Set<String> generatedClients;
  private final Set<String> importTypes = new TreeSet<>();
  private Append writer;
  private final JavaFileObject fileObject;
  private final String fullName;

  SimpleComponentWriter(Set<String> generatedClients) throws IOException {
    this.generatedClients = generatedClients;
    this.fullName = fullName();
    fileObject = createWriter(fullName());
  }

  String fullName() {
    String topPackage = TopPackage.of(generatedClients);
    if (!topPackage.endsWith(".httpclient")) {
      topPackage += ".httpclient";
    }
    return topPackage + ".GeneratedHttpComponent";
  }

  void write() throws IOException {
    writer = new Append(fileObject.openWriter());
    writePackage();
    writeImports();
    writeClassStart();
    writeRegister();
    writeClassEnd();
    writer.close();
    writeMetaInf();
  }

  void writeMetaInf() throws IOException {
    final FileObject fileObject =
        createMetaInfWriter("META-INF/services/io.avaje.http.client.HttpClient$GeneratedComponent");
    if (fileObject != null) {
      try (var fileWriter = fileObject.openWriter()) {
        fileWriter.write(fullName);
      }
    }
  }

  private void writeRegister() {
    writer.append("  @Override").eol();
    writer.append("  public void register(Map<Class<?>, HttpApiProvider<?>> providerMap) {").eol();

    for (final String clientFullName : generatedClients) {

      final String clientShortName = Util.shortName(clientFullName);
      final var clientInterface = removeLast(clientShortName, "HttpClient");
      writer
          .append("    providerMap.put(%s.class, %s::new);", clientInterface, clientShortName)
          .eol();
    }
    writer.append("  }").eol().eol();
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    final String shortName = Util.shortName(fullName);
    writer.append(AT_GENERATED).eol();
    writer
        .append("public class %s implements HttpClient.GeneratedComponent {", shortName)
        .eol()
        .eol();
  }

  private void writeImports() {
    importTypes.add("io.avaje.http.client.HttpClient");
    importTypes.add("io.avaje.http.client.HttpApiProvider");
    importTypes.add("java.util.Map");
    importTypes.add("io.avaje.http.api.Generated");

    importTypes.addAll(generatedClients);
    generatedClients.stream()
        .map(s -> removeLast(removeLast(s, ".httpclient"), "HttpClient"))
        .forEach(importTypes::add);

    for (final String importType : importTypes) {
      writer.append("import %s;", importType).eol();
      writer.append("import %s;", importType).eol();
    }
    writer.eol();
  }

  public static String removeLast(String s, String search) {
    final int pos = s.lastIndexOf(search);

    if (pos > -1) {
      return s.substring(0, pos) + s.substring(pos + search.length());
    }

    return s;
  }

  private void writePackage() {
    final String packageName = TopPackage.packageOf(fullName);
    if (packageName != null && !packageName.isEmpty()) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }
}
