package io.avaje.http.generator.core.openapi;

import java.util.Optional;

import io.avaje.http.generator.core.ConsumesPrism;
import io.avaje.http.generator.core.CoreWebMethod;
import io.avaje.http.generator.core.HiddenPrism;
import io.avaje.http.generator.core.MethodParam;
import io.avaje.http.generator.core.MethodReader;
import io.avaje.http.generator.core.SecurityRequirementPrism;
import io.avaje.http.generator.core.javadoc.Javadoc;
import io.avaje.prism.GeneratePrism;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;

/** Build the OpenAPI documentation for a method. */
@GeneratePrism(Deprecated.class)
public class MethodDocBuilder {

  private final Javadoc javadoc;
  private final MethodReader methodReader;
  private final DocContext ctx;

  private final Operation operation = new Operation();
  private final Optional<ConsumesPrism> consumeOp;

  public MethodDocBuilder(MethodReader methodReader, DocContext ctx) {
    this.methodReader = methodReader;
    this.ctx = ctx;
    this.javadoc = methodReader.javadoc();
    this.consumeOp = methodReader.consumesAnnotation();
  }

  public void build() {
    if (ctx.isOpenApiAvailable() && methodReader.findAnnotation(HiddenPrism::getOptionalOn).isPresent()) {
      return;
    }

    methodReader.readOperation(operation, javadoc);
    operation.setTags(methodReader.tags());

    if (javadoc.isDeprecated()
        || methodReader.findAnnotation(DeprecatedPrism::getOptionalOn).isPresent()) {
      operation.setDeprecated(true);
    }

    final PathItem pathItem = ctx.pathItem(methodReader.fullPath());
    switch ((CoreWebMethod) methodReader.webMethod()) {
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

    final var securityRequirements = methodReader.securityRequirements();
    for (final SecurityRequirementPrism p : securityRequirements) {
        final var o = new SecurityRequirement().addList(p.name(), p.scopes());
        operation.addSecurityItem(o);
    }

    for (final MethodParam param : methodReader.params()) {
      param.buildApiDocumentation(this);
    }

    final ApiResponses responses = new ApiResponses();
    operation.setResponses(responses);

    final ApiResponse response = new ApiResponse();
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
      if (responseAnnotation.responseCode().toString().startsWith("2")) {
        newResponse.setContent(response.getContent());
        override2xx = !hasProducesStatus;
      }
      final var returnType = responseAnnotation.type();

      if (!"java.lang.Void".equals(returnType.toString())) {
        newResponse.setContent(ctx.createContent(returnType, contentMediaType));
      }

      responses.addApiResponse(responseAnnotation.responseCode().toString(), newResponse);
    }
    if (!override2xx) responses.addApiResponse(String.valueOf(methodReader.statusCode()), response);
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

  public Optional<ConsumesPrism> consumeOp() {
    return consumeOp;
  }

  private boolean isEmpty(String value) {
    return value == null || value.isEmpty();
  }
}
