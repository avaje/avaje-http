package io.avaje.http.generator.client;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.ClientPrism;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.MethodReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Write Http client adapter.
 */
final class ClientWriter extends BaseControllerWriter {

  private static final String HTTP_CLIENT = "io.avaje.http.client.HttpClient";

  private static final String AT_GENERATED = "@Generated(\"avaje-http-client-generator\")";

  private final List<ClientMethodWriter> methodList = new ArrayList<>();
  private final Set<String> propertyConstants = new HashSet<>();
  private final String suffix;

  private final boolean packagePrivate;

  ClientWriter(ControllerReader reader, String suffix, boolean packagePrivate) throws IOException {
    super(reader, suffix);
    this.suffix = suffix;
    this.packagePrivate = packagePrivate;
    reader.addImportType(HTTP_CLIENT);
    readMethods();
  }

  @Override
  protected String initPackageName(TypeElement originName) {
    // put the generated Http client into a sub-package
    final var beanType = reader.beanType();

    String packageName = super.initPackageName(originName);
    return !beanType.getModifiers().contains(Modifier.PUBLIC) && ClientPrism.isPresent(beanType)
        ? packageName
        : packageName.isBlank() ? packageName : packageName + ".httpclient";
  }

  private void readMethods() {
    for (final MethodReader method : reader.methods()) {
      if (method.isWebMethod()) {
        final var methodWriter = new ClientMethodWriter(method, writer, propertyConstants);
        methodWriter.addImportTypes(reader);
        methodList.add(methodWriter);
      }
    }
  }

  String write() {
    writePackage();
    writeImports();
    writeClassStart();
    writeMethods();
    writeClassEnd();
    return fullName;
  }

  private void writeMethods() {
    for (final ClientMethodWriter methodWriter : methodList) {
      methodWriter.write();
    }
    writer.append("  @Override").eol();
    writer.append("  public void close() {").eol();
    writer.append("    this.client.close();").eol();
    writer.append("  }").eol();
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    AnnotationUtil.writeAnnotations(writer, reader.beanType());
    var access = packagePrivate ? "" : "public ";
    writer.append("%sfinal class %s%s implements %s, AutoCloseable {", access, shortName, suffix, shortName).eol().eol();

    writer.append("  private final HttpClient client;").eol().eol();

    writer.append("  %s%s%s(HttpClient client) {", access, shortName, suffix).eol();
    writer.append("    this.client = client;").eol();
    writer.append("  }").eol().eol();
  }

}
