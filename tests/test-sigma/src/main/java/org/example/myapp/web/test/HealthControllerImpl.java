package org.example.myapp.web.test;

import io.avaje.http.api.Controller;

@Controller
public class HealthControllerImpl implements HealthController {

  @Override
  public String health() {

    return "this feels like a picnic *chew*";
  }
}
