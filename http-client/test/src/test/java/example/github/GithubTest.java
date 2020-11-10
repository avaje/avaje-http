package example.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.avaje.http.client.BodyAdapter;
import io.avaje.http.client.HttpApi;
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
      .withBaseUrl("https://api.github.com")
      .withBodyAdapter(bodyAdapter)
      .withResponseListener(new RequestLogger())
      .build();

    final Simple simple = HttpApi.provide(Simple.class, clientContext);

    final List<Repo> repos = simple.listRepos("rbygrave", "junk");
    assertThat(repos).isNotEmpty();
  }

  private BodyAdapter jacksonBodyAdapter() {
    return new JacksonBodyAdapter(new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
  }

  private BodyAdapter gsonBodyAdapter() {
    return new GsonBodyAdapter(new Gson());
  }
}
