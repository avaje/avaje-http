package org.example.myapp.web;

import io.avaje.validation.constraints.Email;
import io.avaje.validation.constraints.NotNull;
import io.avaje.validation.constraints.Size;
import io.avaje.validation.constraints.Valid;

@Valid
public class GetBeanForm {

  @NotNull @Size(min = 2, max = 150)
  String name;

  @Email @Size(max = 100)
  String email;

  public GetBeanForm(String name, String email) {
    this.name = name;
    this.email = email;
  }

  @Override
  public String toString() {
    return "HelloForm{" +
      "name='" + name + '\'' +
      ", email='" + email + '\'' +
      '}';
  }
}
