package org.example.myapp.web;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Post;

@Controller
public class SwaggerTestController {

  @Get("a")
  StandardRecordWithComments getA() {
    return null;
  }

  @Get("b")
  RecordWithSchemaDescriptions getB() {
    return null;
  }

  @Post("c")
  void postC(RecordWithSchemaImplementation someOtherImpl) {

  }
}
