package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.http.client.HttpApiProvider;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;

import org.example.httpclient.GitHubUsersHttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleTest {

  @Disabled
  @Test
  void listRepos() {

    final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    final HttpClientContext clientContext =
      HttpClientContext.newBuilder()
        .baseUrl("https://api.github.com")
        .bodyAdapter(new JacksonBodyAdapter(objectMapper))
        .build();

    GitHubUsers simple = clientContext.create(GitHubUsers.class);

    final List<Repo> repos = simple.listRepos("rbygrave");
    System.out.println("got repos - " + repos.size());

    assertThat(repos).hasSizeGreaterThan(5);
  }

  public static class AP implements HttpApiProvider<GitHubUsers> {

    @Override
    public Class<GitHubUsers> type() {
      return GitHubUsers.class;
    }

    @Override
    public GitHubUsers provide(HttpClientContext client) {
      return new GitHubUsersHttpClient(client);
    }
  }
}
