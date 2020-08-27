package org.example.webserver;

//import org.hibernate.validator.constraints.URL;

import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Valid
public class HelloForm {

  @NotNull @Size(min = 2, max = 150)
  String name;

  @Email @Size(max = 100)
  String email;

  @URL
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
