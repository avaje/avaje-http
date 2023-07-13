package org.example.myapp.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
