package example.github.httpclient;

import java.util.List;

import example.github.Repo;
import example.github.Simple;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpException;

/** This code could be generated from the interface definition. */
public class Simple$HttpClient implements Simple {

  private final HttpClient context;

  public Simple$HttpClient(HttpClient context) {
    this.context = context;
  }

  // @Get("users/{user}/repos")
  @Override
  public List<Repo> listRepos(String user, String other) throws HttpException {
    return context
        .request()
        .path("users")
        .path(user)
        .path("repos")
        .queryParam("other", other)
        .GET()
        .list(Repo.class);
  }
}
