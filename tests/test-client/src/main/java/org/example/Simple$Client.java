package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class Simple$Client implements Simple {

  private final RsClient client;

  public Simple$Client() {
    HttpClient httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(Duration.ofSeconds(15))
      .build();

    this.client = new RsClient(httpClient, "https://api.github.com/");
  }

  @Override
  public List<Repo> listRepos(String user, String other) throws IOException, InterruptedException {

    String uri = String.format("users/%s/repos", user);
    if (other != null) {
      uri += "?other="+other;
    }

    //client.get(uri);
    return null;
  }

  @Override
  public Repo getById(String id) {
    return null;
  }

  @Override
  public HttpResponse<Repo> getById2(String id) {
    return null;
  }

  @Override
  public void readUsing(String id, Consumer<Stream<String>> consumer) {

  }

  @Override
  public void downloadMe(String id, Consumer<InputStream> file) {

  }
}
