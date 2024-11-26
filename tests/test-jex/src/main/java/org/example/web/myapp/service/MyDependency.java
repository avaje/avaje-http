package org.example.web.myapp.service;

import jakarta.inject.Singleton;

@Singleton
public class MyDependency {

  public String hello() {
    return "my dependency";
  }
}
