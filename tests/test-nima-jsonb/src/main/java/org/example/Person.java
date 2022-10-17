package org.example;

import io.avaje.jsonb.Json;

@Json
public record Person(long id, String name) {

}
