package org.example.myapp.web;

import io.avaje.http.api.Body;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;

@Controller
@Path("/enum")
public class EnumController {

  @Get("/first")
  public EnumDTO first() {
    return new EnumDTO(EnumExample.ENUM_VALUE_1, EnumExample.ENUM_VALUE_2);
  }

  @Get("/second")
  public EnumDTO second(EnumExample enumExample) {
    return new EnumDTO(EnumExample.ENUM_VALUE_1, EnumExample.ENUM_VALUE_2);
  }

  @Post("/third")
  public EnumDTO third(@Body EnumExample enumExample) {
    return new EnumDTO(EnumExample.ENUM_VALUE_1, EnumExample.ENUM_VALUE_2);
  }

  @Post("/fourth")
  public EnumDTO fourth(EnumDTO enumDTO) {
    return new EnumDTO(EnumExample.ENUM_VALUE_1, EnumExample.ENUM_VALUE_2);
  }
}
