package io.avaje.http.generator.core;

import java.io.IOException;
import java.io.Writer;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/** Common controller writer. */
public abstract class BaseControllerWriter {

  protected final ControllerReader reader;
  protected final ProcessingContext ctx;
  protected final String originName;
  protected final String shortName;
  protected final String fullName;
  protected final String packageName;
  protected final boolean router;
  protected Append writer;

  protected BaseControllerWriter(ControllerReader reader, ProcessingContext ctx)
      throws IOException {
    this(reader, ctx, "$Route");
  }

  protected BaseControllerWriter(ControllerReader reader, ProcessingContext ctx, String suffix)
      throws IOException {
    this.reader = reader;
    this.ctx = ctx;
    this.router = "$Route".equals(suffix);
    TypeElement origin = reader.beanType();
    this.originName = origin.getQualifiedName().toString();
    this.shortName = origin.getSimpleName().toString();
    this.packageName = initPackageName(originName);
    this.fullName = packageName + "." + shortName + suffix;

    initWriter();
  }

  protected boolean isRequestScoped() {
    return reader.isRequestScoped();
  }

  protected String initPackageName(String originName) {
    int dp = originName.lastIndexOf('.');
    return dp > -1 ? originName.substring(0, dp) : null;
  }

  protected void initWriter() throws IOException {
    writer = new Append(createFileWriter());
  }

  protected Writer createFileWriter() throws IOException {
    JavaFileObject jfo = ctx.createWriter(fullName, reader.beanType());
    return jfo.openWriter();
  }

  protected void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }

  protected void writeImports() {
    if (router) {
      writer.append(Constants.IMPORT_PATH_TYPE_CONVERT).eol();
    }
    for (String type : reader.staticImportTypes()) {
      writer.append("import static %s;", type).eol();
    }
    writer.eol();
    for (String type : reader.importTypes()) {
      writer.append("import %s;", type).eol();
    }
    writer.eol();
  }

  protected void writeClassEnd() {
    writer.append("}").eol();
    writer.close();
  }
}
