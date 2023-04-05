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
  private Optional<ConsumesPrism> consumeOp;

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
      param.setName(varName);
      param.setDescription(javadoc.getParams().get(paramName));

      Schema schema = ctx.toSchema(rawType, element);
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

    Schema schema = ctx.toSchema(rawType, element);
    String description = javadoc.getParams().get(paramName);
    var mediaType =
        consumeOp
            .map(ConsumesPrism::value)
            .orElseGet(
                () -> {
                  boolean asForm = (paramType == ParamType.FORM);
                  var mime = asForm ? APP_FORM : APP_JSON;

                  if (schema instanceof StringSchema) {
                    mime = APP_TXT;
                  }
                  return mime;
                });

    ctx.addRequestBody(operation, schema, mediaType, description);
  }

}
