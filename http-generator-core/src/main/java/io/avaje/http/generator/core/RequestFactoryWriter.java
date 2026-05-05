package io.avaje.http.generator.core;

import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static io.avaje.http.generator.core.ProcessingContext.createWriter;
import static io.avaje.http.generator.core.ProcessingContext.platform;
import static io.avaje.http.generator.core.ProcessingContext.useComponent;
import static io.avaje.http.generator.core.ProcessingContext.useJavax;

/**
 * Generates a {@code $RequestFactory} class for request-scoped controllers.
 * <p>
 * The generated class is a {@code @Singleton} with {@code @Inject} fields for DI dependencies,
 * and a {@code create(...)} method that takes the request-scoped arguments and returns a new
 * controller instance per request.
 */
final class RequestFactoryWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-http-generator\")";

  private final ControllerReader reader;
  private final String packageName;
  private final String shortName;
  private final Append writer;

  /** Constructor params that are DI dependencies (not request-scoped). */
  private final List<VariableElement> diConstructorParams = new ArrayList<>();

  RequestFactoryWriter(ControllerReader reader) throws IOException {
    this.reader = reader;
    this.packageName = APContext.elements().getPackageOf(reader.beanType()).getQualifiedName().toString();
    this.shortName = reader.beanType().getSimpleName().toString();

    var fullName = packageName.isBlank()
      ? shortName + Constants.REQUEST_FACTORY_SUFFIX
      : packageName + "." + shortName + Constants.REQUEST_FACTORY_SUFFIX;

    classifyConstructorParams();

    JavaFileObject jfo = createWriter(fullName, reader.beanType());
    this.writer = new Append(jfo.openWriter());
  }

  private void classifyConstructorParams() {
    for (var param : reader.constructorParams()) {
      String rawType = param.asType().toString();
      if (!RequestScopeTypes.isRequestType(rawType)) {
        diConstructorParams.add(param);
      }
    }
  }

  void write() {
    writePackage();
    writeImports();
    writeClassStart();
    writeDiFields();
    writeCreateMethod();
    writeClassEnd();
    writer.close();
  }

  private void writePackage() {
    if (packageName != null && !packageName.isBlank()) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }

  private void writeImports() {
    var imports = new TreeSet<String>();
    imports.add("io.avaje.inject.spi.Generated");

    // DI annotation
    if (useComponent()) {
      imports.add("io.avaje.inject.Component");
    } else {
      imports.add(useJavax() ? "javax.inject.Singleton" : "jakarta.inject.Singleton");
    }

    // Inject annotation for fields
    if (!diConstructorParams.isEmpty() || !reader.diFields().isEmpty()) {
      imports.add(useJavax() ? "javax.inject.Inject" : "jakarta.inject.Inject");
    }

    // controller type
    String controllerFqn = reader.beanType().getQualifiedName().toString();
    if (!samePackage(controllerFqn)) {
      imports.add(controllerFqn);
    }

    // all param and field types
    for (var param : reader.constructorParams()) {
      addImport(imports, param);
    }
    for (var field : reader.diFields()) {
      addImport(imports, field);
    }
    for (var field : reader.requestScopeFields()) {
      addImport(imports, field);
    }
    // platform types for create method signature
    for (String platformType : platform().requestFactoryImportTypes()) {
      if (!samePackage(platformType)) {
        imports.add(platformType);
      }
    }

    for (String imp : imports) {
      writer.append("import %s;", imp).eol();
    }
    writer.eol();
  }

  private boolean samePackage(String fqn) {
    int lastDot = fqn.lastIndexOf('.');
    if (lastDot < 0) return packageName.isBlank();
    return fqn.substring(0, lastDot).equals(packageName);
  }

  private void addImport(TreeSet<String> imports, VariableElement element) {
    String rawType = rawMainType(element);
    if (rawType.contains(".") && !rawType.startsWith("java.lang.") && !samePackage(rawType)) {
      imports.add(rawType);
    }
  }

  /** Return the main type, stripping generics. */
  private String rawMainType(VariableElement element) {
    String type = element.asType().toString();
    int angleBracket = type.indexOf('<');
    return angleBracket > 0 ? type.substring(0, angleBracket) : type;
  }

  /** Return the short type name for a variable element. */
  private String shortType(VariableElement element) {
    String type = element.asType().toString();
    return Util.shortName(type);
  }

  private void writeClassStart() {
    writer.append("@SuppressWarnings(\"all\")").eol();
    writer.append(AT_GENERATED).eol();
    if (useComponent()) {
      writer.append("@Component").eol();
    } else {
      writer.append("@Singleton").eol();
    }
    writer.append("public final class %s%s {", shortName, Constants.REQUEST_FACTORY_SUFFIX).eol().eol();
  }

  private void writeDiFields() {
    // DI constructor params become @Inject fields on the factory
    for (var param : diConstructorParams) {
      writer.append("  @Inject").eol();
      writer.append("  %s %s;", shortType(param), param.getSimpleName()).eol().eol();
    }
    // DI inject fields from the controller
    for (var field : reader.diFields()) {
      writer.append("  @Inject").eol();
      writer.append("  %s %s;", shortType(field), field.getSimpleName()).eol().eol();
    }
  }

  private void writeCreateMethod() {
    // The create method signature matches the platform (what $Route will call)
    String createParams = platform().requestFactoryCreateParams();

    writer.append("  /** Create a new controller instance per request. */").eol();
    writer.append("  public %s create(%s) {", shortName, createParams).eol();

    // construct the bean
    if (reader.constructorParams().isEmpty()) {
      // default constructor
      writer.append("    var bean = new %s();", shortName).eol();
    } else {
      // constructor with params — pass DI fields and request-scoped args by matching param names
      writer.append("    var bean = new %s(", shortName);
      var params = reader.constructorParams();
      for (int i = 0; i < params.size(); i++) {
        if (i > 0) writer.append(", ");
        var param = params.get(i);
        String paramType = param.asType().toString();
        if (RequestScopeTypes.isRequestType(paramType)) {
          // use the platform variable name for this type
          writer.append("%s", platform().platformVariable(paramType));
        } else {
          // use the DI field name
          writer.append("%s", param.getSimpleName());
        }
      }
      writer.append(");").eol();
    }

    // set request-scoped fields (non-final only, final fields are set via constructor)
    for (var field : reader.requestScopeFields()) {
      String fieldType = field.asType().toString();
      writer.append("    bean.%s = %s;", field.getSimpleName(), platform().platformVariable(fieldType)).eol();
    }
    // set DI fields (non-final only, final fields are set via constructor)
    for (var field : reader.diFields()) {
      writer.append("    bean.%s = %s;", field.getSimpleName(), field.getSimpleName()).eol();
    }

    writer.append("    return bean;").eol();
    writer.append("  }").eol();
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }
}
