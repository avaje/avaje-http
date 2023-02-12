package io.avaje.http.generator.core.openapi;

import io.avaje.http.generator.core.HiddenPrism;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.javadoc.Javadoc;
import io.avaje.prism.GeneratePrism;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

/** Build the OpenAPI documentation for a method. */
@GeneratePrism(Deprecated.class)
public class MethodDocBuilder {

  private final Javadoc javadoc;
  private final MethodReader methodReader;
  private final DocContext ctx;

  private final Operation operation = new Operation();

  public MethodDocBuilder(MethodReader methodReader, DocContext ctx) {
    this.methodReader = methodReader;
    this.ctx = ctx;
    this.javadoc = methodReader.javadoc();
  }

  public void build() {

    if (ctx.isOpenApiAvailable()
        && methodReader.findAnnotation(HiddenPrism::getOptionalOn).isPresent()) {
      return;
    }

    //operation.setOperationId();
    operation.setSummary(javadoc.getSummary());
    operation.setDescription(javadoc.getDescription());
    operation.setTags(methodReader.tags());

    if (javadoc.isDeprecated()
        || methodReader.findAnnotation(DeprecatedPrism::getOptionalOn).isPresent()) {
      operation.setDeprecated(true);
    }

    PathItem pathItem = ctx.pathItem(methodReader.fullPath());
    switch (methodReader.webMethod()) {
      case GET:
        pathItem.setGet(operation);
        break;
      case PUT:
        pathItem.setPut(operation);
        break;
      case POST:
        pathItem.setPost(operation);
        break;
      case DELETE:
        pathItem.setDelete(operation);
        break;
      case PATCH:
        pathItem.setPatch(operation);
        break;
    }

    for (MethodParam param : methodReader.params()) {
      param.buildApiDocumentation(this);
    }

    ApiResponses responses = new ApiResponses();
    operation.setResponses(responses);

    ApiResponse response = new ApiResponse();
    response.setDescription(javadoc.getReturnDescription());

    final var produces = methodReader.produces();
    final var hasProducesStatus = methodReader.hasProducesStatus();
    final var contentMediaType = (produces == null) ? "application/json" : produces;

    if (methodReader.isVoid()) {
      if (isEmpty(response.getDescription())) {
        response.setDescription("No content");
      }
    } else {
    	response.setContent(ctx.createContent(methodReader.returnType(), contentMediaType));
    }
    var override2xx = false;
    for (final var responseAnnotation : methodReader.apiResponses()) {
      final var newResponse = new ApiResponse();

      if (responseAnnotation.description().isEmpty()) {
        newResponse.setDescription(response.getDescription());
      } else {
        newResponse.setDescription(responseAnnotation.description());
      }

      // if user wants to define their own 2xx status code
      if (responseAnnotation.responseCode().startsWith("2")) {
        newResponse.setContent(response.getContent());
        override2xx = !hasProducesStatus;
      }
      final var returnType = responseAnnotation.type();

      if (!"java.lang.Void".equals(returnType.toString())) {
        newResponse.setContent(ctx.createContent(returnType, contentMediaType));
      }

      responses.addApiResponse(responseAnnotation.responseCode(), newResponse);
    }
    if (!override2xx) responses.addApiResponse(methodReader.statusCode(), response);
  }

  DocContext getContext() {
    return ctx;
  }

  Javadoc getJavadoc() {
    return javadoc;
  }

  Operation getOperation() {
    return operation;
  }

  private boolean isEmpty(String value) {
    return value == null || value.isEmpty();
  }
}
