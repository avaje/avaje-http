package io.avaje.http.generator.core;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;


public class TestClientWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-http-generator\")";
  private final Set<String> importTypes = new TreeSet<>();
  private final ControllerReader reader;
  private String originName;
  private String shortName;
  private String packageName;
  private String fullName;
  private Append writer;

  TestClientWriter(ControllerReader reader) throws IOException {

    this.reader = reader;
    final TypeElement origin = reader.beanType();
    this.originName = origin.getQualifiedName().toString();
    this.shortName = origin.getSimpleName().toString();
    this.packageName = initPackageName(originName);
    this.fullName = packageName + "." + shortName + "$TestAPI";
    writer = new Append(APContext.createSourceFile(fullName, reader.beanType()).openWriter());
  }

  protected String initPackageName(String originName) {
    final int dp = originName.lastIndexOf('.');
    return dp > -1 ? originName.substring(0, dp) : null;
  }

  void write() {
    writePackage();
    writeImports();
    writeClassStart();
    writeAddRoutes();
  }

  protected void writePackage() {
    if (packageName != null) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }

  protected void writeImports() {
    importTypes.add("java.net.http.HttpResponse");

    reader
        .methods()
        .forEach(
            m -> {
              importTypes.addAll(UType.parse(m.returnType()).importTypes());
              m.params()
                  .forEach(
                      p -> importTypes.addAll(UType.parse(p.element().asType()).importTypes()));
            });

    importTypes.addAll(reader.importTypes());

    importTypes.removeIf(
        i ->
            i.startsWith("java.lang")
                || PrimitiveUtil.wrapperMap.containsKey(i)
                || i.contains(".") && i.substring(0, i.lastIndexOf(".")).equals(packageName));
    for (String type : importTypes) {
      writer.append("import %s;", type).eol();
    }
    writer.eol();
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    writer.append("@Client(\"%s\")", reader.path()).eol();
    writer.append("public interface %s$TestAPI {", shortName).eol().eol();
  }

  private void writeAddRoutes() {

    reader.methods().stream()
        .filter(MethodReader::isWebMethod)
        .filter(
            m ->
                m.webMethod() instanceof CoreWebMethod
                    && m.webMethod() != CoreWebMethod.ERROR
                    && m.webMethod() != CoreWebMethod.FILTER
                    && m.webMethod() != CoreWebMethod.OTHER)
        .forEach(this::writeRoute);

    writer.append("}").eol();
    writer.close();
  }

  private void writeRoute(MethodReader method) {
    TypeMirror returnType = method.returnType();
    var isJstache = ProcessingContext.isJstacheTemplate(returnType);
    AnnotationCopier.copyAnnotations(writer, method.element(), true);

    var returnTypeStr = PrimitiveUtil.wrap(UType.parse(returnType).shortType());
    writer.append(
        "HttpResponse<%s> %s(", isJstache ? "String" : returnTypeStr, method.simpleName());
    boolean first = true;
    for (var param : method.params()) {

      if (first) {
        first = false;
      } else {
        writer.append(", ");
      }
      var type = UType.parse(param.element().asType());

      AnnotationCopier.copyAnnotations(writer, param.element(), false);
      writer.append("%s %s", type.shortType(), param.name());
    }
    writer.append(");");

    writer.eol();
    writer.eol();
  }
}
