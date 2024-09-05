package org.example.myapp.web.test;

import io.avaje.http.api.Get;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.OpenAPIResponse;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("javalin")
@OpenAPIDefinition(
    info =
        @Info(
            title = "Example service showing off the Path extension method of controller",
            description = ""))
@OpenAPIResponse(responseCode = 403, description = "Not Authorized")
public interface HealthController {
  /**
   * Standard Get
   *
   * @return a health check
   */
  @Get("/health")
  @Produces(MediaType.TEXT_PLAIN)
  @Tag(name = "tag1", description = "it's somethin")
  @OpenAPIResponse(responseCode = 500, type = ErrorResponse.class)
  String health();
}
