package io.avaje.http.generator.helidon.nima;

import java.util.List;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.UType;

class NimaPlatformAdapter implements PlatformAdapter {

  static final String NIMA_REQ = "io.helidon.nima.webserver.http.ServerRequest";
  static final String NIMA_RES = "io.helidon.nima.webserver.http.ServerResponse";
  static final String HELIDON_FORMPARAMS = "io.helidon.common.parameters.Parameters";

  @Override
  public boolean isContextType(String rawType) {
    return NIMA_REQ.equals(rawType)
        || NIMA_RES.equals(rawType)
        || HELIDON_FORMPARAMS.equals(rawType);
  }

  @Override
  public String platformVariable(String rawType) {
    if (NIMA_REQ.equals(rawType)) {
      return "req";
    }
    if (NIMA_RES.equals(rawType)) {
      return "res";
    }
    if (HELIDON_FORMPARAMS.equals(rawType)) {
      return "formParams";
    }
    return "unknownVariable for: " + rawType;
  }

  @Override
  public boolean isBodyMethodParam() {
    return true;
  }

  @Override
  public String bodyAsClass(UType uType) {
    return "body";
  }

  @Override
  public String indent() {
    return "  ";
  }

  @Override
  public void controllerRoles(List<String> roles, ControllerReader controller) {
    addRoleImports(roles, controller);
  }

  @Override
  public void methodRoles(List<String> roles, ControllerReader controller) {
    addRoleImports(roles, controller);
  }

  private void addRoleImports(List<String> roles, ControllerReader controller) {
    // nothing here yet
  }

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName) {
    switch (paramType) {
      case PATHPARAM -> writer.append("pathParams.first(\"%s\").get()", paramName);

      case QUERYPARAM -> writer.append("req.query().first(\"%s\").orElse(null)", paramName);

      case FORMPARAM -> writer.append("formParams.first(\"%s\").orElse(null)", paramName);

      case HEADER -> writer.append(
          "req.headers().value(Header.create(\"%s\")).orElse(null)", paramName);

      case COOKIE -> writer.append("req.headers().cookies().first(\"%s\").orElse(null)", paramName);

      default -> writer.append("null // TODO req.%s().param(\"%s\")", paramType.type(), paramName);
    }
  }

  @Override
  public void writeReadParameter(
      Append writer, ParamType paramType, String paramName, String paramDefault) {
    switch (paramType) {
      case PATHPARAM -> writer.append(
          "pathParams.first(\"%s\").orElse(\"%s\")", paramName, paramDefault);

      case QUERYPARAM -> writer.append(
          "req.query().first(\"%s\").orElse(\"%s\")", paramName, paramDefault);

      case FORMPARAM -> writer.append(
          "formParams.first(\"%s\").orElse(\"%s\")", paramName, paramDefault);

      case HEADER -> writer.append(
          "req.headers().value(Http.Header.create(\"%s\").orElse(\"%s\")", paramName, paramDefault);

      case COOKIE -> writer.append(
          "req.headers().cookies().first(\"%s\").orElse(\"%s\")", paramName, paramDefault);

      default -> writer.append("null // TODO req.%s().param(\"%s\")", paramType.type(), paramName);
    }
  }

  @Override
  public void writeReadMapParameter(Append writer, ParamType paramType) {
    switch (paramType) {
      case QUERYPARAM -> writer.append("req.query().toMap()");
      case FORM, FORMPARAM -> writer.append("formParams.toMap()");
      case COOKIE -> writer.append("req.headers().cookies().toMap()");
      default -> throw new UnsupportedOperationException(
          "Only Form/Query/Cookie Multi-Value Maps are supported");
    }
  }

  @Override
  public void writeReadCollectionParameter(Append writer, ParamType paramType, String paramName) {
    switch (paramType) {
      case QUERYPARAM -> writer.append("req.query().all(\"%s\")", paramName);
      case FORMPARAM -> writer.append("formParams.all(\"%s\")", paramName);

      case HEADER -> writer.append(
          "req.headers().all(\"%s\", () -> java.util.List.of())", paramName);

      case COOKIE -> writer.append(
          "req.headers().cookies().all(\"%s\", () -> java.util.List.of())", paramName);

      default -> throw new UnsupportedOperationException(
          "Only (Form/Query/Header/Cookie) List Parameters are supported for Helidon");
    }
  }

  @Override
  public void writeReadCollectionParameter(
      Append writer, ParamType paramType, String paramName, List<String> paramDefault) {
    switch (paramType) {
      case QUERYPARAM -> writer.append(
          "req.query().all(\"%s\", () -> java.util.List.of(\"%s\"))",
          paramName, String.join(",", paramDefault));

      case FORMPARAM -> writer.append(
          "formParams.all(\"%s\", () -> java.util.List.of(\"%s\"))",
          paramName, String.join(",", paramDefault));

      case HEADER -> writer.append(
          "req.headers().all(\"%s\", () -> java.util.List.of(\"%s\"))",
          paramName, String.join(",", paramDefault));

      case COOKIE -> writer.append(
          "req.headers().cookies().all(\"%s\", () -> java.util.List.of(\"%s\"))",
          paramName, String.join(",", paramDefault));

      default -> throw new UnsupportedOperationException(
          "Only (Form/Query/Header/Cookie) List Parameters are supported for Helidon");
    }
  }

  @Override
  public void writeAcceptLanguage(Append writer) {
    writer.append("language(req)");
  }
}
