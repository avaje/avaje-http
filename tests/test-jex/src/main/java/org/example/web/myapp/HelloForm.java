package org.example.web.myapp;

import java.time.LocalDate;

import io.avaje.http.api.Valid;
import io.avaje.jsonb.Json;
import io.avaje.validation.constraints.Email;
import io.avaje.validation.constraints.Future;
import io.avaje.validation.constraints.NotNull;
import io.avaje.validation.constraints.Size;
import io.avaje.validation.constraints.URI;

@Json
@Valid
public class HelloForm {

  @NotNull
  @Size(min = 2, max = 150)
  String name;

  @Email
  @Size(max = 100)
  String email;

  @URI
  private String url;

  @Future
  public LocalDate startDate;

  public HelloForm(String name, String email) {
    this.name = name;
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  @Override
  public String toString() {
    return "HelloForm{"
        + "name='"
        + name
        + '\''
        + ", email='"
        + email
        + '\''
        + ", url='"
        + url
        + '\''
        + ", startDate="
        + startDate
        + '}';
  }
}
