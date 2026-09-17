package org.example.myapp.web;

import io.avaje.http.api.FormParam;

public class ContactForm {

  @FormParam("phone")
  public String phone;

  @FormParam("email")
  public String email;
}
