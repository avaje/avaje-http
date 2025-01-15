package io.avaje.http.generator.client;

import io.avaje.http.generator.core.APContext;
import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.MethodReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Write Http client adapter.
 */
final class ClientWriter extends BaseControllerWriter {

  private static final String HTTP_CLIENT = "io.avaje.http.client.HttpClient";

  private static final String AT_GENERATED = "@Generated(\"avaje-http-client-generator\")";

  private final List<ClientMethodWriter> methodList = new ArrayList<>();
  private final boolean useJsonb;
  private final Set<String> propertyConstants = new HashSet<>();
  private final String suffix;

  ClientWriter(ControllerReader reader, String suffix, boolean useJsonB) throws IOException {
    super(reader, suffix);
    this.suffix = suffix;
    reader.addImportType(HTTP_CLIENT);
    this.useJsonb = useJsonB;
    readMethods();
    if (useJsonB) reader.addImportType("io.avaje.jsonb.Types");
  }

  @Override
  protected String initPackageName(String originName) {
    // put the generated Http client into a sub-package
    return super.initPackageName(originName) + ".httpclient";
  }

  private void readMethods() {
    for (final MethodReader method : reader.methods()) {
      if (method.isWebMethod()) {
        final var methodWriter = new ClientMethodWriter(method, writer, useJsonb, propertyConstants);
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

    writer.append("public final class %s%s implements %s, AutoCloseable {", shortName, suffix, shortName).eol().eol();

    writer.append("  private final HttpClient client;").eol().eol();

    writer.append("  public %s%s(HttpClient client) {", shortName, suffix).eol();
    writer.append("    this.client = client;").eol();
    writer.append("  }").eol().eol();
  }

}
