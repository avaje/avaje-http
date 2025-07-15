package org.example.web;

import io.avaje.jsonb.Json;
import io.avaje.validation.constraints.NotNull;
import io.avaje.validation.constraints.Valid;

@Valid
@Json
public class HelloDto {
  public int id;
  @NotNull
  public String name;
  public ServerType serverType;

  public HelloDto() {
  }

  public HelloDto(int id, String name) {
    this.id = id;
    this.name = name;
  }
}
