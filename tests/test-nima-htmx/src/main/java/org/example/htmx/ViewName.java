package org.example.htmx;

import io.jstach.jstache.JStache;

import java.time.Instant;
import java.util.List;

@JStache(path = "name")
public record ViewName(String name, Instant foo, String more, List<String> mlist) {
  public String when() {
    return foo.toString();
  }
}
