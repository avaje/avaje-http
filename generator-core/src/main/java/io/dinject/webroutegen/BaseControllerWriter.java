package io.dinject.webroutegen;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

/**
 * Common controller writer.
 */
public abstract class BaseControllerWriter {

  protected final ControllerReader reader;
  protected final ProcessingContext ctx;
  protected final String originName;
  protected final String shortName;
  protected final String fullName;
  protected final String packageName;
  protected Append writer;

  public BaseControllerWriter(ControllerReader reader, ProcessingContext ctx) throws IOException {
    this.reader = reader;
    this.ctx = ctx;
    TypeElement origin = reader.getBeanType();
    this.originName = origin.getQualifiedName().toString();
    this.shortName = origin.getSimpleName().toString();
    this.fullName = originName + "$route";
    this.packageName = initPackageName(originName);

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
    JavaFileObject jfo = ctx.createWriter(fullName, reader.getBeanType());
    return jfo.openWriter();
  }

  protected void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }

  protected void writeImports() {
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

  protected void writeClassEnd() {
    writer.append("}").eol();
    writer.close();
  }
}
