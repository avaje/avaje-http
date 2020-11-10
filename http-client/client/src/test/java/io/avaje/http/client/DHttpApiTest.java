package io.avaje.http.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.github.Repo;
import org.example.github.Simple;
import org.example.github.SimpleHttpClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DHttpApiTest {

  @Test
  void test_github_listRepos() {

    JacksonBodyAdapter bodyAdapter = new JacksonBodyAdapter(new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));

    final HttpClientContext clientContext = HttpClientContext.newBuilder()
      .withBaseUrl("https://api.github.com")
      .withBodyAdapter(bodyAdapter)
      .build();

    DHttpApi httpApi = new DHttpApi();
    httpApi.addProvider(new SimpleHttpClient.Provider());
    final Simple simple = httpApi.provideFor(Simple.class, clientContext);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

}
