package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import io.avaje.http.client.HttpClient;

public class ContractControllerTest {

  private static TestPair pair = new TestPair();
  private static HttpClient client = pair.client();

  @AfterAll
  static void end() {
    pair.stop();
  }

  @Test
  void getUser() {
    HttpResponse<String> res = client.request()
      .path("contract/users/42")
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("user:42");
  }

  @Test
  void getRepo_swappedParameterOrder() {
    // Route is /contract/repos/{org}/{repo}, method signature is (repo, org)
    // Name-based matching ensures "avaje" is org and "avaje-http" is repo
    HttpResponse<String> res = client.request()
      .path("contract/repos/avaje/avaje-http")
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("org:avaje,repo:avaje-http");
  }

  @Test
  void search_namedAndBareQueryParams() {
    HttpResponse<String> res = client.request()
      .path("contract/search")
      .queryParam("q", "hello")
      .queryParam("filter", "active")
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("q:hello,filter:active");
  }

  @Test
  void userRoles_pathAndQueryParams() {
    HttpResponse<String> res = client.request()
      .path("contract/orgs/my-org/users/rob/roles")
      .queryParam("activeOnly", true)
      .GET()
      .asString();

    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).isEqualTo("org:my-org,user:rob,activeOnly:true");
  }
}
