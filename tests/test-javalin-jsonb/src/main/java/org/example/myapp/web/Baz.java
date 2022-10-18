package org.example.myapp.web;

import java.time.LocalDate;

import io.avaje.jsonb.Json;
@Json
public class Baz {

  public Long id;

  public String name;

  public LocalDate startDate;

}
