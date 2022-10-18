package org.example.myapp.web;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

import io.avaje.jsonb.Json;

@Json
@Valid
public class HelloForm {

  @NotNull
  @Size(min = 2, max = 150)
  String name;

  @Email
  @Size(max = 100)
  String email;
@URL
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
