package org.example.myapp.service;

import javax.inject.Singleton;

@Singleton
public class MyDependency {

  public String hello() {
    return "my dependency";
  }
}
