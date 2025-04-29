package io.avaje.http.generator.core.openapi;

import java.util.Optional;

import javax.lang.model.element.Element;

import io.avaje.http.generator.core.ConsumesPrism;
import io.avaje.http.generator.core.ElementReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.javadoc.Javadoc;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * Build the OpenAPI for a method parameter.
 */
public class MethodParamDocBuilder {
  private static final String APP_FORM = "application/x-www-form-urlencoded";
  private static final String APP_JSON = "application/json";
  private static final String APP_TXT = "application/text";
  private final DocContext ctx;
  private final Javadoc javadoc;
  private final Operation operation;
  private final String paramName;
  private final String varName;
  private final String rawType;
  private final ParamType paramType;
  private final Element element;
  private final Optional<ConsumesPrism> consumeOp;

  public MethodParamDocBuilder(MethodDocBuilder methodDoc, ElementReader param) {
    this.ctx = methodDoc.getContext();
    this.javadoc = methodDoc.getJavadoc();
    this.operation = methodDoc.getOperation();
    this.consumeOp = methodDoc.consumeOp();
    this.paramType = param.paramType();
    this.paramName = param.paramName();
    this.varName = param.varName();
    this.rawType = param.type().full();
    this.element = param.element();
  }

  /**
   * Build the OpenAPI documentation for the method parameter.
   */
  public void build() {
    if (paramType == ParamType.FORM || paramType == ParamType.BODY) {
      addMetaRequestBody(ctx, javadoc, operation);

    } else {
      Parameter param = new Parameter();
      param.setName(paramName);
      param.setDescription(javadoc.getParams().get(paramName));

      Schema<?> schema = ctx.toSchema(rawType, element);
      if (paramType == ParamType.FORMPARAM) {
        ctx.addFormParam(operation, varName, schema);

      } else {
        param.setSchema(schema);
        param.setIn(paramType.type());
        operation.addParametersItem(param);
      }
    }
  }

  private void addMetaRequestBody(DocContext ctx, Javadoc javadoc, Operation operation) {
    final var schema = ctx.toSchema(rawType, element);
    final var description = javadoc.getParams().get(paramName);
    var mediaType =
        consumeOp
            .map(ConsumesPrism::value)
            .orElseGet(() -> requestMedia(schema));

    ctx.addRequestBody(operation, schema, mediaType, description);
  }

  private String requestMedia(Schema<?> schema) {
    if (schema instanceof StringSchema) {
      return APP_TXT;
    }
    boolean asForm = paramType == ParamType.FORM;
    return asForm ? APP_FORM : APP_JSON;
  }

}
