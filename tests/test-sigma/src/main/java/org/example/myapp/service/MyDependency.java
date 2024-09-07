package org.example.myapp.service;

import jakarta.inject.Singleton;

@Singleton
public class MyDependency {

  public String hello() {
    return "my dependency";
  }
}
