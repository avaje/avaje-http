package example.github.pkgprivate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;
import io.avaje.jex.Jex;

class PkgPrivateTest {

  static Jex.Server server = null;
  String url = "http://localhost:" + server.port();

  @BeforeAll
  static void startServer() {
    server = Jex.create().get("/private", ctx -> ctx.text("private")).port(0).start();
  }

  @AfterAll
  static void stop() {
    server.shutdown();
  }

  @Test
  void test_create() {
    final HttpClient client = HttpClient.builder().baseUrl("https://api.github.com").build();

    final var simple = client.create(SimplePkgPrivate.class);
    assertThat(simple).isNotNull();
  }

  @Test
  void test_pkg_private() {
    final HttpClient client = HttpClient.builder().baseUrl(url).build();

    final var simple = client.create(SimplePkgPrivate.class);

    final var result = simple.get();
    assertThat(result).isNotEmpty();
  }
}
