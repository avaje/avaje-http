package io.avaje.http.client;

import org.example.github.Repo;
import org.example.github.Simple;
import org.example.github.httpclient.Simple$HttpClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DHttpApiTest {

  @Test
  void test_github_listRepos() {

    final HttpClientContext clientContext = HttpClientContext.newBuilder()
      .baseUrl("https://api.github.com")
      .bodyAdapter(new JacksonBodyAdapter())
      .build();

    DHttpApi httpApi = new DHttpApi();
    httpApi.addProvider(new Simple$HttpClient.Provider());
    final Simple simple = httpApi.provideFor(Simple.class, clientContext);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

}
