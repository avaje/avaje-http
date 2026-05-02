package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Post;
import io.avaje.http.api.Path;

import org.jspecify.annotations.NullMarked;

@NullMarked
@Controller
@Path("/nullmarked")
public class NullMarkedController {

  @Get("/record")
  public NullMarkedRecordDTO findAllRecords() {

    return new NullMarkedRecordDTO("A", null);
  }

  @Get("/class")
  public NullMarkedClassDTO findAll() {
    return new NullMarkedClassDTO();
  }

  @Post("/class")
  public void post(NullMarkedClassDTO dto) {
  }

}
