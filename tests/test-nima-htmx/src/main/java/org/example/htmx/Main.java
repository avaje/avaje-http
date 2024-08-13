package org.example.htmx;

import io.avaje.inject.InjectModule;
import io.avaje.nima.Nima;

@InjectModule(name = "hxTest")
public class Main {

  public static void main(String[] args) {
    Nima.builder()
      .port(8090)
      .build()
      .start();
  }
}
