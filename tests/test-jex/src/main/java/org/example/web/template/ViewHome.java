package org.example.web.template;

import io.jstach.jstache.JStache;

@JStache(path = "home")
public class ViewHome {
  public final String name;

  public ViewHome(String name) {
    this.name = name;
  }
}
