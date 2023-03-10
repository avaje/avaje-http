package org.example.web;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Valid
public class HelloDto {
  public int id;
  @NotNull
  public String name;
  public ServerType serverType;
}
