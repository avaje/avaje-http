package io.dinject.javalin.generator;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

class ControllerRouteWriter {

  private final ControllerReader reader;

  private final ProcessingContext ctx;

  private final String originName;
  private final String shortName;
  private final String fullName;
  private String packageName;
  private Append writer;

  ControllerRouteWriter(ControllerReader reader, ProcessingContext ctx) {
    this.reader = reader;
    this.ctx = ctx;

    TypeElement origin = reader.getBeanType();
    originName = origin.getQualifiedName().toString();
    shortName = origin.getSimpleName().toString();
    fullName = originName + "$route";
  }

  void write() throws IOException {

    writer = new Append(createFileWriter());
    writePackage();
    writeImports();
    writeClassStart();

    writeAddRoutes();

    writeClassEnd();
    writer.close();
  }

  private void writeAddRoutes() {
    writer.append("  @Override").eol();
    writer.append("  public void registerRoutes() {").eol().eol();

    for (MethodReader method : reader.getMethods()) {
      method.addRoute(writer);
      method.addMeta(ctx);
    }

    writer.append("  }").eol().eol();
  }

  private void writeImports() {

    writer.append(Constants.IMPORT_PATH_TYPE_CONVERT).eol();
    for (String type : reader.getStaticImportTypes()) {
      writer.append("import static %s;", type).eol();
    }
    writer.eol();
    for (String type : reader.getImportTypes()) {
      writer.append("import %s;", type).eol();
    }
    writer.eol();
  }


  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {

    if (ctx.isGeneratedAvailable()) {
      writer.append(Constants.AT_GENERATED).eol();
    }
    writer.append("@Singleton").eol();
    writer.append("public class ").append(shortName).append("$route implements WebRoutes {").eol().eol();

    writer.append(" private final %s controller;", shortName).eol().eol();

    writer.append(" public %s$route(%s controller) {", shortName, shortName).eol();
    writer.append("   this.controller = controller;", shortName, shortName).eol();
    writer.append(" }", shortName, shortName).eol().eol();
  }

  private void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }

  private Writer createFileWriter() throws IOException {

    int dp = originName.lastIndexOf('.');
    if (dp > -1) {
      packageName = originName.substring(0, dp);
    }

    JavaFileObject jfo = ctx.createWriter(fullName, reader.getBeanType());
    return jfo.openWriter();
  }
}
