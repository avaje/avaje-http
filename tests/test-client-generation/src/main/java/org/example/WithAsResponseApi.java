package org.example;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.client.HttpCall;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Client
public interface WithAsResponseApi {

  @Get("/{id}")
  Repo get(UUID id);

  @Get("/as/{id}")
  HttpResponse<Repo> getAs(UUID id);

  @Get("/as-list/{id}")
  HttpResponse<List<Repo>> getAsList(UUID id);

  @Get("/as-stream/{id}")
  HttpResponse<Stream<Repo>> getAsStream(UUID id);

  @Get("/call-as/{id}")
  HttpCall<HttpResponse<Repo>> getCallAs(UUID id);

  @Get("/call-as-list/{id}")
  HttpCall<HttpResponse<List<Repo>>> getCallAsList(UUID id);

  @Get("/call-as-stream/{id}")
  HttpCall<HttpResponse<Stream<Repo>>> getCallAsStream(UUID id);
}
