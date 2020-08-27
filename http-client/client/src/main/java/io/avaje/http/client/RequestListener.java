package io.avaje.http.client;

import java.net.URI;
import java.net.http.HttpResponse;

public interface RequestListener {

  void response(Event event);

  interface Event {

    long requestTimeNanos();

    HttpResponse<?> response();

    String requestBody();

    String responseBody();

    URI uri();
  }
}
