package org.example.webserver;

//import org.hibernate.validator.constraints.URL;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Valid
public class HelloForm {

  @NotNull @Size(min = 2, max = 150)
  public String name;

  @Email @Size(max = 100)
  public String email;

  @URL
  public String url;

  @Future
  public LocalDate startDate;

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
