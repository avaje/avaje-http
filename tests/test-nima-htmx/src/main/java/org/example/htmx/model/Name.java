package org.example.htmx.model;

import io.jstach.jstache.JStache;

import java.time.Instant;
import java.util.List;

@JStache(path = "ui/name.mustache")
public record Name(String name, Instant foo, String more, List<String> mlist) {
  public String when() {
    return foo.toString();
  }

  public record Pair(String nm, int eg) {}
}
