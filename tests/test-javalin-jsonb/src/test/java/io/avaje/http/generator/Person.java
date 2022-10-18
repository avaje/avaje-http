package io.avaje.http.generator;

public class Person {
  private final long id;
  private final String name;

  public Person(long id, String name) {
    this.id = id;
    this.name = name;
  }

  public long id() {
    return id;
  }

  public String name() {
    return name;
  }
}
