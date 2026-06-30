package org.example;

import io.avaje.http.api.Client;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;
import org.example.server.CommonControllerTestAPI;
import org.example.server.Main;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Client.Import(CommonApi.class)
class CommonApiTest {

  static HttpClient clientContext;
  static CommonApi client;

  @BeforeAll
  static void start() {

    final int port = new Random().nextInt(1000) + 10_000;
    Main.start(port);

    clientContext = HttpClient.builder()
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

  @Test
  void testApi_generatedForContractFirstController_returnsHttpResponse() {
    // CommonController implements the @Path/@Get annotated CommonApi interface (contract-first),
    // so the generated CommonControllerTestAPI exposes the full HttpResponse (status + headers).
    final CommonControllerTestAPI testApi = clientContext.create(CommonControllerTestAPI.class);

    final HttpResponse<String> hello = testApi.hello();
    assertThat(hello.statusCode()).isEqualTo(200);
    assertThat(hello.body()).isEqualTo("hello world");

    final HttpResponse<String> name = testApi.name("foo");
    assertThat(name.statusCode()).isEqualTo(200);
    assertThat(name.body()).isEqualTo("name[foo]");

    // query params (@QueryParam names copied from the interface) and path params
    final LocalDate date = LocalDate.of(2021, 6, 24);
    final HttpResponse<String> p2 = testApi.p2(42, "foo", date, false);
    assertThat(p2.statusCode()).isEqualTo(200);
    assertThat(p2.body()).isEqualTo("p2[42;foo; after:2021-06-24 more:false]");

    final HttpResponse<String> p2NullQuery = testApi.p2(44, "bar", null, true);
    assertThat(p2NullQuery.statusCode()).isEqualTo(200);
    assertThat(p2NullQuery.body()).isEqualTo("p2[44;bar; after:null more:true]");
  }
}
