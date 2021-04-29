package org.example;

import io.avaje.http.client.HttpClientContext;
import io.avaje.http.api.QueryParam;

import java.util.List;


public class Simple$htclient {//implements Simple {

  final HttpClientContext clientContext;

  public Simple$htclient(HttpClientContext clientContext) {
    this.clientContext = clientContext;
  }

  //@Get("users/{user}/repos")
//  @Override
  public List<Repo> listRepos(String user, @QueryParam("foo-too") String fooToo) {
    return clientContext.request()
      .path("users").path(user).path("repos").queryParam("foo-too", fooToo)
      .get()
      .list(Repo.class);
  }

}
