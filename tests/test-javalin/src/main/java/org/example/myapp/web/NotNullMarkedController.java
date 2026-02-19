package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import org.jspecify.annotations.NullMarked;

@Controller
@Path("/notnullmarked")
public class NotNullMarkedController {

  @Get("/record")
  public NotNullMarkedRecordDTO findAllRecords() {

    return new NotNullMarkedRecordDTO("A", null);
  }

  @Get("/class")
  public NotNullMarkedClassDTO findAll() {
    return new NotNullMarkedClassDTO();
  }

  @Post("/class")
  public void post(NotNullMarkedClassDTO dto) {
  }

}
