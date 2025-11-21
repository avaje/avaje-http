package org.example.myapp.web.test;

import io.avaje.http.api.Header;

public class MyForm {

  public String name;
  public String email;
  public String url;
  @Header
  public String headString;
}
