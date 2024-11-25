package org.example.web;

import io.avaje.validation.constraints.NotNull;
import io.avaje.validation.constraints.Valid;

@Valid
public class HelloDto {
  public int id;
  @NotNull
  public String name;
  public ServerType serverType;
}
