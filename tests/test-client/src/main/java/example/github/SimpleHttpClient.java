package example.github;

import io.avaje.http.client.HttpApiProvider;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.HttpException;

import java.util.List;

/**
 * This code could be generated from the interface definition.
 */
public class SimpleHttpClient implements HttpApiProvider<Simple> {

  @Override
  public Class<Simple> type() {
    return Simple.class;
  }

  @Override
  public Simple provide(HttpClient client) {
    return new SimpleClient(client);
  }

  private static class SimpleClient implements Simple {

    private final HttpClient context;

    SimpleClient(HttpClient context) {
      this.context = context;
    }

    //@Get("users/{user}/repos")
    @Override
    public List<Repo> listRepos(String user, String other) throws HttpException {
      return context.request()
        .path("users").path(user).path("repos")
        .queryParam("other", other)
        .GET().list(Repo.class);
    }

  }

}
