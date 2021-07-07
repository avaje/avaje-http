package example.github;

import com.google.gson.Gson;
import io.avaje.http.client.BodyAdapter;
import io.avaje.http.client.HttpClientContext;
import io.avaje.http.client.JacksonBodyAdapter;
import io.avaje.http.client.RequestLogger;
import io.avaje.http.client.gson.GsonBodyAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GithubTest {

  @Test
  void test_with_jackson() {
    assertListRepos(jacksonBodyAdapter());
  }

  @Test
  void test_with_gson() {
    assertListRepos(gsonBodyAdapter());
  }

  private void assertListRepos(BodyAdapter bodyAdapter) {
    final HttpClientContext clientContext = HttpClientContext.newBuilder()
      .baseUrl("https://api.github.com")
      .bodyAdapter(bodyAdapter)
      .requestListener(new RequestLogger())
      .build();

    final Simple simple = clientContext.create(Simple.class);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

  private BodyAdapter jacksonBodyAdapter() {
    return new JacksonBodyAdapter();
  }

  private BodyAdapter gsonBodyAdapter() {
    return new GsonBodyAdapter(new Gson());
  }
}
