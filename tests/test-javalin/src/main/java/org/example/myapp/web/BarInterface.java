package org.example.myapp.web;

import io.avaje.http.api.Get;
import io.avaje.http.api.MediaType;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
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
  @Get(instrumentRequestContext = true)
  String barMessage();
}
