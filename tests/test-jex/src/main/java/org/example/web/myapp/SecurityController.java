package org.example.web.myapp;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Controller
@Path("/security")
class SecurityController {

  @Get("/first")
  @SecurityRequirement(name = "JWT")
  String first() {
    return "simple";
  }

  @Get("/second")
  @SecurityRoles
  String second() {
    return "simple";
  }
}
