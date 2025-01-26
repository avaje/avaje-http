package example.github;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import io.avaje.http.client.BodyAdapter;
import io.avaje.http.client.HttpClient;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.http.client.gson.GsonBodyAdapter;
import io.avaje.jex.Jex;

class GithubTest {

  static Jex.Server server = null;
  String url = "http://localhost:" + server.port();

  @BeforeAll
  static void startServer() {
    server =
        Jex.create()
            .get(
                "/users/{user}/repos",
                ctx -> {
                  var repo = new Repo();
                  repo.setId(1);
                  repo.setName("something");
                  ctx.json(List.<Repo>of(new Repo()));
                })
            .post(
                "/generic",
                ctx -> {
                  var repo = new Repo();
                  repo.setId(1);
                  repo.setName("something");
                  var data = new GenericData<Repo>();
                  data.setData(repo);

                  ctx.json(List.<GenericData<Repo>>of(data));
                })
            .port(0)
            .start();
  }

  @AfterAll
  static void stop() {
    server.shutdown();
  }

  @Test
  void test_create() {
    final HttpClient client = HttpClient.builder().baseUrl("https://api.github.com").build();

    final Simple simple = client.create(Simple.class);
    assertThat(simple).isNotNull();
  }

  @Test
  void test_with_jackson() {
    assertListRepos(jacksonBodyAdapter());
  }

  @Test
  void testGeneric_with_jackson() {
    assertGeneric(jacksonBodyAdapter());
  }

  @Test
  void test_with_gson() {
    assertListRepos(gsonBodyAdapter());
  }

  private void assertListRepos(BodyAdapter bodyAdapter) {
    final HttpClient client =
        HttpClient.builder()
            .baseUrl(url)
            .bodyAdapter(bodyAdapter)
            //      .requestLogging(false)
            //      .requestListener(new RequestLogger())
            .build();

    final Simple simple = client.create(Simple.class);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

  private void assertGeneric(BodyAdapter bodyAdapter) {
    final HttpClient client =
        HttpClient.builder()
            .baseUrl(url)
            .bodyAdapter(bodyAdapter)
            //      .requestLogging(false)
            //      .requestListener(new RequestLogger())
            .build();

    final var generic = client.create(Generic.class);

    var repo = new Repo();
    repo.setId(1);
    repo.setName("something");
    var data = new GenericData<Repo>();
    data.setData(repo);

    final var repos = generic.post(data);
    assertThat(repos).isNotEmpty();
  }

  private BodyAdapter jacksonBodyAdapter() {
    return new JacksonBodyAdapter();
  }

  private BodyAdapter gsonBodyAdapter() {
    return new GsonBodyAdapter(new Gson());
  }
}
