package org.example.htmx;

import io.jstach.jstache.JStache;

@JStache(path = "home")
public record ViewHome(String name) {
}
