package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.http.client.RequestLogger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTest {

  @Test
  void listRepos() {

    final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    final HttpClientContext clientContext =
      HttpClientContext.newBuilder()
        .withBaseUrl("https://api.github.com")
        .withRequestListener(new RequestLogger())
        .withBodyAdapter(new JacksonBodyAdapter(objectMapper))
        .build();

    GitHubUsers simple = clientContext.create(GitHubUsers.class);

    final List<Repo> repos = simple.listRepos("rbygrave");
    System.out.println("got repos - " + repos.size());

    assertThat(repos).hasSizeGreaterThan(5);
  }

}
