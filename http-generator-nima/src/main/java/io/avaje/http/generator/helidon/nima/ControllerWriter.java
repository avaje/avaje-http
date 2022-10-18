package io.avaje.http.generator.helidon.nima;

import java.io.IOException;
import java.util.*;

import javax.lang.model.type.TypeMirror;

import io.avaje.http.generator.core.*;

/** Write Helidon specific web route adapter (a Helidon Service). */
class ControllerWriter extends BaseControllerWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-helidon-nima-generator\")";
  private final boolean useJsonB;
  private final Map<String, UType> jsonTypes = new LinkedHashMap<>();

  ControllerWriter(ControllerReader reader, ProcessingContext ctx, boolean jsonB) throws IOException {
    super(reader, ctx);
    this.useJsonB = jsonB;
    if (useJsonB) {
      reader.addImportType("io.avaje.jsonb.Jsonb");
      reader.addImportType("io.avaje.jsonb.JsonType");
      initJsonTypes(reader);
    }
    reader.addImportType("io.helidon.common.http.HttpMediaType");
    reader.addImportType("io.helidon.common.parameters.Parameters");
    reader.addImportType("io.helidon.nima.webserver.http.HttpRules");
    reader.addImportType("io.helidon.nima.webserver.http.HttpRouting");
    reader.addImportType("io.helidon.nima.webserver.http.ServerRequest");
    reader.addImportType("io.helidon.nima.webserver.http.ServerResponse");
    reader.addImportType("io.helidon.nima.webserver.http.HttpService");
  }

  private void initJsonTypes(ControllerReader reader) {
      reader.getMethods().stream()
        .filter(MethodReader::isWebMethod)
        .filter(m -> !"byte[]".equals(m.getReturnType().toString()))
        .filter(m -> m.getProduces() == null || m.getProduces().toLowerCase().contains("json"))
        .forEach(methodReader -> {
          addJsonBodyType(methodReader);
          if (!methodReader.isVoid()) {
            TypeMirror returnType = methodReader.getReturnType();
            addJsonBodyType(Util.parseType(returnType));
          }
        });
  }

  private void addJsonBodyType(UType type) {
    jsonTypes.put(type.full(), type);
  }

  private void addJsonBodyType(MethodReader methodReader) {
    if (methodReader.getBodyType() != null) {
      methodReader.getParams().stream()
        .filter(MethodParam::isBody)
        .forEach(param -> addJsonBodyType(param.getUType()));
    }
  }

  void write() {
    writePackage();
    writeImports();
    writeClassStart();
    writeAddRoutes();
    writeClassEnd();
  }

  private List<ControllerMethodWriter> getWriterMethods() {
    return reader.getMethods().stream()
        .filter(MethodReader::isWebMethod)
        .map(it -> new ControllerMethodWriter(it, writer, ctx, useJsonB, jsonTypes))
        .toList();
  }

  private void writeAddRoutes() {
    final var methods = getWriterMethods();
    writeRoutes(methods);
    for (final ControllerMethodWriter methodWriter : methods) {
      methodWriter.writeHandler(isRequestScoped());
    }
  }

  private void writeRoutes(List<ControllerMethodWriter> methods) {
    writer.append("  @Override").eol();
    writer.append("  public void routing(HttpRules rules) {").eol();

    for (final ControllerMethodWriter methodWriter : methods) {
      methodWriter.writeRule();
      if (!reader.isDocHidden()) {
        methodWriter.buildApiDocumentation();
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeClassStart() {
    writer.append(AT_GENERATED).eol();
    writer.append("@Component").eol();
    writer.append("public class %s$Route implements HttpService {", shortName).eol().eol();

    var controllerName = "controller";
    var controllerType = shortName;
    if (isRequestScoped()) {
      controllerName = "factory";
      controllerType += Constants.FACTORY_SUFFIX;
    }
    writer.append("  private final %s %s;", controllerType, controllerName).eol();

    if (reader.isIncludeValidator()) {
      writer.append("  private final Validator validator;").eol();
    }
    for (UType value : jsonTypes.values()) {
      writer.append("private final JsonType<%s> %sJsonType; //RBx1", primitiveWrap(value.full()), value.shortName()).eol();
    }
    writer.eol();

    writer.append("  public %s$Route(%s %s", shortName, controllerType, controllerName);
    if (reader.isIncludeValidator()) {
      writer.append(", Validator validator");
    }
    if (useJsonB) {
      writer.append(", Jsonb jsonB");
    }
    writer.append(") {").eol();
    writer.append("    this.%s = %s;", controllerName, controllerName).eol();
    if (reader.isIncludeValidator()) {
      writer.append("    this.validator = validator;").eol();
    }
    if (useJsonB) {
      for (UType value : jsonTypes.values()) {
        writer.append("    this.%sJsonType = jsonB.type(", value.shortName());
        writeJsonbType(value);
      }
    }
    writer.append("  }").eol().eol();
  }

  private void writeJsonbType(UType type) {
    if (!type.isGeneric()) {
      writer.append("%s.class)", type.full());
    } else {
      switch (type.mainType()) {
        case "java.util.List" -> writer.append("%s.class).list()", type.param0());
        case "java.util.Set" -> writer.append("%s.class).set()", type.param0());
        case "java.util.Map" -> writer.append("%s.class).map()", type.param1());
        default -> throw new UnsupportedOperationException("Only java.util Map, Set and List are supported JsonB Controller Collection Types");
      }
    }
    writer.append("; // Rbx3").eol();
  }

  private String primitiveWrap(String full) {
    return PrimitiveUtil.wrap(full);
  }

}
