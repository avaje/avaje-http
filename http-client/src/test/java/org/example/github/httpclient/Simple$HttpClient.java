package org.example.github.httpclient;

import io.avaje.http.api.Get;
import io.avaje.http.api.Post;
import io.avaje.http.client.*;
import org.example.github.Repo;
import org.example.github.Simple;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;


public class Simple$HttpClient implements Simple {

  private final HttpClient context;
  private final BodyReader<Repo> readRepo;
  private final BodyWriter writeRepo;

  public Simple$HttpClient(HttpClient context) {
    this.context = context;
    this.readRepo = context.bodyAdapter().beanReader(Repo.class);
    this.writeRepo = context.bodyAdapter().beanWriter(Repo.class);
  }

  @Get("users/{user}/repos")
  @Override
  public List<Repo> listRepos(String user, String other) throws HttpException {
    return context.request()
      .path("users").path(user).path("repos")
      .queryParam("other", other)
      .GET().list(Repo.class);
  }

  @Post("users")
  @Override
  public Repo post(Repo repo) throws HttpException {
    return context.request()
      .path("foo/users")
      .body(writeRepo.write(repo))
      .POST().read(readRepo);
  }

  @Get("users/{id}")
  @Override
  public Repo getById(String id) {
    return context.request()
      .path("users").path(id)
      .GET().bean(Repo.class);
  }

  public InputStream getById2(String id, InputStream is) {
    final HttpResponse<InputStream> response =
      context.request()
        .path("users").path(id).path("stream")
        .body(() -> is)
        .GET().handler(HttpResponse.BodyHandlers.ofInputStream());

    //context.checkResponse(response);
    return response.body();
  }

  @Override
  public HttpResponse<Repo> getById2(String id) {
    return null;
  }

  public static class Provider implements HttpApiProvider<Simple> {

    @Override
    public Class<Simple> type() {
      return Simple.class;
    }

    @Override
    public Simple provide(HttpClient client) {
      return new Simple$HttpClient(client);
    }
  }

}
