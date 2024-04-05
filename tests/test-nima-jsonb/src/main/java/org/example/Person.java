package org.example;

import io.avaje.jsonb.Json;

/**
 * @param id the id
 * @param name the name
 */
@Json
public record Person(long id, String name) {}
