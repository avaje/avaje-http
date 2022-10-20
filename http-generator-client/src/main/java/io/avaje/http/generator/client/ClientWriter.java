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

  private static final String HTTP_CLIENT_CONTEXT = "io.avaje.http.client.HttpClientContext";
  private static final String HTTP_API_PROVIDER = "io.avaje.http.client.HttpApiProvider";

  private static final String AT_GENERATED = "@Generated(\"avaje-http-client-generator\")";
  private static final String SUFFIX = "HttpClient";

  private final List<ClientMethodWriter> methodList = new ArrayList<>();

  ClientWriter(ControllerReader reader, ProcessingContext ctx) throws IOException {
    super(reader, ctx, SUFFIX);
    reader.addImportType(HTTP_CLIENT_CONTEXT);
    reader.addImportType(HTTP_API_PROVIDER);
    readMethods();
  }

  @Override
  protected String initPackageName(String originName) {
    // put the generated Http client into a sub-package
    return super.initPackageName(originName) + ".httpclient";
  }

  private void readMethods() {
    for (MethodReader method : reader.methods()) {
      if (method.isWebMethod()) {
        ClientMethodWriter methodWriter = new ClientMethodWriter(method, writer, ctx);
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
    writeProvider();
    writeClassEnd();
    return fullName;
  }

  private void writeProvider() {
    writer.append("  public static class Provider implements HttpApiProvider<%s> {", shortName).eol();
    writer.append("    @Override").eol();
    writer.append("    public Class<%s> type() {", shortName).eol();
    writer.append("      return %s.class;", shortName).eol();
    writer.append("    }").eol();
    writer.append("    @Override").eol();
    writer.append("    public %s provide(HttpClientContext client) {", shortName).eol();
    writer.append("      return new %s%s(client);", shortName, SUFFIX).eol();
    writer.append("    }").eol();
    writer.append("  }").eol();
  }

  private void writeMethods() {
    for (ClientMethodWriter methodWriter : methodList) {
      methodWriter.write();
    }
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    writer.append("public class %s%s implements %s {", shortName, SUFFIX, shortName).eol().eol();

    writer.append("  private final HttpClientContext clientContext;").eol().eol();

    writer.append("  public %s%s(HttpClientContext ctx) {", shortName, SUFFIX).eol();
    writer.append("    this.clientContext = ctx;").eol();
    writer.append("  }").eol().eol();
  }

}
