package org.example.myapp.web.test;

import java.util.List;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.OpenAPIResponse;
import io.avaje.http.api.OpenAPIResponses;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.http.api.Put;
import io.javalin.http.Context;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
    info =
        @Info(
            title = "Example service",
            description = "Example Javalin controllers with Java and Maven"))
@Controller
@Path("openapi/")
@SecurityScheme(
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.QUERY,
    name = "JWT",
    paramName = "access_token",
    description =
        "JSON Web Tokens are an open, industry standard RFC 7519 method for representing claims securely between two parties.")
public class OpenAPIController {

  /**
   * Example of Open API Get (up to the first period is the summary). When using Javalin Context
   * only <br>
   * This Javadoc description is added to the generated openapi.json
   *
   * @return funny phrase (this part of the javadoc is added to the response desc)
   */
  @Get("/get")
  @Produces(MediaType.TEXT_PLAIN)
  @OpenAPIResponse(responseCode = 200, type = String.class)
  void ctxEndpoint(Context ctx) {
    ctx.contentType(MediaType.TEXT_PLAIN).result("healthlmao");
  }

  /**
   * Standard Post. uses tag annotation to add tags to openapi json
   *
   * @param b the body (this is used for generated request body desc)
   * @return the response body (from javadoc)
   */
  @Post("/post")
  @Tag(name = "tag1", description = "this is added to openapi tags")
  @OpenAPIResponse(responseCode = 200, description = "overrides @return javadoc description")
  @OpenAPIResponse(responseCode = 201)
  @OpenAPIResponse(
      responseCode = 400,
      description = "User not found (Will not have an associated response schema)")
  @OpenAPIResponse(
      responseCode = 500,
      description = "Some other Error (Will have this error class as the response class)",
      type = ErrorResponse.class)
  Person testPost(Person b) {
    return new Person(0, "baby");
  }

  /**
   * Standard Post. The Deprecated annotation adds "deprecacted:true" to the generated json
   *
   * @param m the body
   * @return the response body (from javadoc)
   */
  @Deprecated
  @Post("/post1")
  @OpenAPIResponses({
    @OpenAPIResponse(responseCode = 400, description = "User not found"),
    @OpenAPIResponse(
        responseCode = 500,
        description = "Some other Error",
        type = ErrorResponse.class)
  })
  Person testPostList(List<Person> m) {
    return new Person(0, "baby");
  }

  @Put("/put")
  @Produces(value = MediaType.TEXT_PLAIN, statusCode = 203)
  @OpenAPIResponse(responseCode = 204, type = String.class)
  String testDefaultStatus(Context ctx) {
    if (ctx.contentType().equals(MediaType.APPLICATION_PDF)) {
      ctx.status(204);
      return "";
    }
    return "only partial info";
  }
}
