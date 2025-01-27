package org.example;

import io.avaje.http.api.Client;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;
import org.example.server.Main;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Client.Import(CommonApi.class)
class CommonApiTest {

  static CommonApi client;

  @BeforeAll
  static void start() {

    final int port = new Random().nextInt(1000) + 10_000;
    Main.start(port);

    final HttpClient clientContext = HttpClient.builder()
      .baseUrl("http://localhost:" + port)
      .bodyAdapter(new JacksonBodyAdapter())
      .build();

    client = clientContext.create(CommonApi.class);
  }

  @Test
  void hello() {
    assertThat(client.hello()).isEqualTo("hello world");
  }

  @Test
  void name() {
    assertThat(client.name("foo")).isEqualTo("name[foo]");
    assertThat(client.name("bar")).isEqualTo("name[bar]");
  }

  @Test
  void p2() {
    final LocalDate date = LocalDate.of(2021, 6, 24);
    final String result = client.p2(42, "foo", date, false);
    assertThat(result).isEqualTo("p2[42;foo; after:2021-06-24 more:false]");

    final String result2 = client.p2(44, "bar", null, true);
    assertThat(result2).isEqualTo("p2[44;bar; after:null more:true]");
  }
}
