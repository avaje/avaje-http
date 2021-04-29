package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.http.client.RequestLogger;
import org.example.httpclient.GitHubUsers$httpclient;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    GitHubUsers simple = new GitHubUsers$httpclient(clientContext);

    final List<Repo> repos = simple.listRepos("rbygrave");
    System.out.println("got repos - " + repos.size());
  }
}
