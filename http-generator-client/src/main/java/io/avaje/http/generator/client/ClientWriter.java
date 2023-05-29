package io.avaje.http.generator.client;

import io.avaje.http.generator.core.BaseControllerWriter;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.ProcessingContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Write Http client adapter.
 */
class ClientWriter extends BaseControllerWriter {

  private static final String HTTP_CLIENT = "io.avaje.http.client.HttpClient";

  private static final String AT_GENERATED = "@Generated(\"avaje-http-client-generator\")";
  private static final String SUFFIX = "HttpClient";

  private final List<ClientMethodWriter> methodList = new ArrayList<>();
  private final boolean useJsonb;

  ClientWriter(ControllerReader reader, boolean useJsonB) throws IOException {
    super(reader, SUFFIX);
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
        final var methodWriter = new ClientMethodWriter(method, writer, useJsonb);
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
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    AnnotationUtil.writeAnnotations(writer, reader.beanType());
    writer.append("public class %s%s implements %s {", shortName, SUFFIX, shortName).eol().eol();

    writer.append("  private final HttpClient client;").eol().eol();

    writer.append("  public %s%s(HttpClient client) {", shortName, SUFFIX).eol();
    writer.append("    this.client = client;").eol();
    writer.append("  }").eol().eol();
  }

}
