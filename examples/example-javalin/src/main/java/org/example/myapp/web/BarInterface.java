package org.example.myapp.web;

import io.dinject.controller.Get;
import io.dinject.controller.MediaType;
import io.dinject.controller.Path;
import io.dinject.controller.Produces;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.List;

@Path("/bars")
public interface BarInterface {

  @Get(":id")
  @ApiResponse(links = @Link(name="find", ref ="/find/:code", description="find by code"))
  Bar getById(long id);

  @Get("/find/:code")
  List<Bar> findByCode(String code);

  @Produces(MediaType.TEXT_PLAIN)
  @Get
  String barMessage();
}
