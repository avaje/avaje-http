package io.avaje.http.generator.client.clients;

import io.avaje.http.client.HttpException;

public class MappedException extends RuntimeException {

  public MappedException(HttpException e) {}
}
