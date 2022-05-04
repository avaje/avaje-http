package io.avaje.http.client;

import io.avaje.jsonb.Jsonb;
import org.example.github.Repo;
import org.example.github.Simple;
import org.example.github.httpclient.Simple$HttpClient;
import org.example.github.RepoJsonAdapter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DHttpApiTest {

  @Disabled
  @Test
  void test_github_listRepos() {

    final HttpClientContext clientContext = HttpClientContext.builder()
      .baseUrl("https://api.github.com")
      .bodyAdapter(new JacksonBodyAdapter())
      .build();

    DHttpApi httpApi = new DHttpApi();
    httpApi.addProvider(new Simple$HttpClient.Provider());
    final Simple simple = httpApi.provideFor(Simple.class, clientContext);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

  @Test
  void jsonb_github_listRepos() {

    Jsonb jsonb = Jsonb.newBuilder()
      .add(Repo.class, RepoJsonAdapter::new)
      //.adapter(new JacksonAdapter())
      .build();

    final HttpClientContext clientContext = HttpClientContext.builder()
      .baseUrl("https://api.github.com")
      .bodyAdapter(new JsonbBodyAdapter(jsonb))
      .build();

    DHttpApi httpApi = new DHttpApi();
    httpApi.addProvider(new Simple$HttpClient.Provider());
    final Simple simple = httpApi.provideFor(Simple.class, clientContext);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

}
