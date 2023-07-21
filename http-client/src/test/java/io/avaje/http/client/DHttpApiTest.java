package io.avaje.http.client;

import io.avaje.jsonb.Jsonb;
import org.example.github.Repo;
import org.example.github.RepoJsonAdapter;
import org.example.github.Simple;
import org.example.github.httpclient.Simple$HttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DHttpApiTest {

  @Disabled
  @Test
  void test_github_listRepos() {

    final var clientContext = HttpClient.builder()
      .baseUrl("https://api.github.com")
      .bodyAdapter(new JacksonBodyAdapter())
      .build();

    DHttpApi httpApi = new DHttpApi();
    httpApi.addProvider(Simple.class, Simple$HttpClient::new);
    final Simple simple = httpApi.provideFor(Simple.class, clientContext);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

  @Disabled
  @Test
  void jsonb_github_listRepos() {

    Jsonb jsonb = Jsonb.builder()
      .add(Repo.class, RepoJsonAdapter::new)
      .build();

    final HttpClient client = HttpClient.builder()
      .baseUrl("https://api.github.com")
      .bodyAdapter(new JsonbBodyAdapter(jsonb))
      .build();

    DHttpApi httpApi = new DHttpApi();
    httpApi.addProvider(Simple.class, Simple$HttpClient::new);
    final Simple simple = httpApi.provideFor(Simple.class, client);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

}
