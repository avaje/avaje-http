package org.example.web.template;

import io.jstach.jstache.JStache;

@JStache(path = "partial")
public class ViewPartial {
  public final String name;

  public ViewPartial(String name) {
    this.name = name;
  }
}
