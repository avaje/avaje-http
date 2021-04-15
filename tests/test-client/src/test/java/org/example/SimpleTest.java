package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.http.client.RequestLogger;
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
        //.withResponseListener(new RequestLogger())
        .withBodyAdapter(new JacksonBodyAdapter(objectMapper))
        .build();

    Simple simple = new Simple$httpclient(clientContext);

    final List<Repo> repos = simple.listRepos("octocat", "rbygrave");
    System.out.println("got repos - " + repos.size());
  }
}
