package org.example.myapp.web;

import java.time.LocalDate;

import io.avaje.validation.constraints.Email;
import io.avaje.validation.constraints.Future;
import io.avaje.validation.constraints.NotNull;
import io.avaje.validation.constraints.Size;
import io.avaje.validation.constraints.URI;
import io.avaje.validation.constraints.Valid;

@Valid
public class HelloForm {

  @NotNull @Size(min = 2, max = 150)
  String name;

  @Email @Size(max = 100)
  String email;

  @URI
  String url;

  @Future
  LocalDate startDate;

  public HelloForm(String name, String email) {
    this.name = name;
    this.email = email;
  }

  @Override
  public String toString() {
    return "HelloForm{" +
      "name='" + name + '\'' +
      ", email='" + email + '\'' +
      ", url='" + url + '\'' +
      ", startDate=" + startDate +
      '}';
  }
}
