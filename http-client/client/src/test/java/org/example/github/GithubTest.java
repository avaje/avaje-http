package org.example.github;

import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GithubTest {

  @Test
  @Disabled
  void test() throws InterruptedException {

    final HttpClientContext clientContext = HttpClientContext.newBuilder()
      .baseUrl("https://api.github.com")
      .bodyAdapter(new JacksonBodyAdapter())
      .requestLogging(false)
      .build();

    clientContext.request()
      .path("users").path("rbygrave").path("repos")
      .GET()
      .async()
      .asString()
      .thenAccept(res -> {

        System.out.println("RES: "+res.statusCode());
        System.out.println("BODY: "+res.body());
      });

    Thread.sleep(1_000);

    // will not work under module classpath without registering the HttpApiProvider
    final Simple simple = clientContext.create(Simple.class);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

}
