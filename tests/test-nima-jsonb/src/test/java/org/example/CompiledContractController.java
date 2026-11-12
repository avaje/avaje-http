package org.example;

import io.avaje.http.api.Controller;

@Controller
public class CompiledContractController implements CompiledContractApi {

  @Override
  public Person echo(Person request) {
    return request;
  }
}
