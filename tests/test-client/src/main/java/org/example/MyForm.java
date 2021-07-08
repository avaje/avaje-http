package org.example;

import io.avaje.http.api.Header;

import java.time.LocalDate;

public class MyForm {

  public String name;
  public String email;
  public LocalDate started;

  @Header("MFoo")
  public String fooHead;

  public String email() {
    return email;
  }
}
