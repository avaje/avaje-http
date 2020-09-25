package org.example;

import io.avaje.http.client.BodyReader;
import io.avaje.http.client.BodyWriter;
import io.avaje.http.client.HttpApiProvider;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.HttpException;
import io.dinject.controller.Get;
import io.dinject.controller.Post;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;


class Simple$rc implements Simple {

  private final HttpClientContext context;
  private final BodyReader<Repo> readRepo;
  private final BodyWriter writeRepo;
//  private final BodyConverter<List<Repo>, String> toListOfRepo;

  Simple$rc(HttpClientContext context) {
    this.context = context;
    this.readRepo = context.converters().beanReader(Repo.class);
    this.writeRepo = context.converters().beanWriter(Repo.class);
//    this.toListOfRepo = context.converters().toListOf(Repo.class);
  }

  @Get("users/{user}/repos")
  @Override
  public List<Repo> listRepos(String user, String other) throws HttpException {
    return context.request()
      .path("users").path(user).path("repos")
      .queryParam("other", other)
      .get().list(Repo.class);
  }

  @Post("users")
  @Override
  public Repo post(Repo repo) throws HttpException {
    return context.request()
      .path("foo/users")
      .body(writeRepo.write(repo))
      .post().read(readRepo);
  }

  @Get("users/{id}")
  @Override
  public Repo getById(String id) {
    return context.request()
      .path("users").path(id)
      .get().bean(Repo.class);
  }

  public InputStream getById2(String id, InputStream is) {
    final HttpResponse<InputStream> response =
      context.request()
        .path("users").path(id).path("stream")
        .body(() -> is)
        .get().withResponseHandler(HttpResponse.BodyHandlers.ofInputStream());

    context.checkResponse(response);
    return response.body();
  }

  @Override
  public HttpResponse<Repo> getById2(String id) {
    return null;
  }

  static class Provider implements HttpApiProvider<Simple> {
    @Override
    public Simple provide(HttpClientContext client) {
      return new Simple$rc(client);
    }
  }

}
