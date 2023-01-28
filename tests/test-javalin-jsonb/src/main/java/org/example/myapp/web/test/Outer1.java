package org.example.myapp.web.test;

import io.avaje.jsonb.Json;

public interface Outer1 {

  @Json
  class State {

    private String field;

    public State() {}

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }

    public State(final String field) {
      this.field = field;
    }
  }
}
