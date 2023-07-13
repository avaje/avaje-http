package org.example.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Valid
public class HelloDto {
  public int id;
  @NotNull
  public String name;
  public ServerType serverType;
}
