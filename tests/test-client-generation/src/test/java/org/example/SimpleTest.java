package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTest {

  @Disabled
  @Test
  void listRepos() {
    final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    final HttpClient client =
      HttpClient.builder()
        .baseUrl("https://api.github.com")
        .bodyAdapter(new JacksonBodyAdapter(objectMapper))
        .build();

    final GitHubUsers simple = client.create(GitHubUsers.class);

    final List<Repo> repos = simple.listRepos("rbygrave");
    System.out.println("got repos - " + repos.size());

    assertThat(repos).hasSizeGreaterThan(5);
  }

}
