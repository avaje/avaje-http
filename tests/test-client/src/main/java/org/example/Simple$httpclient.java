package org.example;

import io.avaje.http.client.HttpClientContext;
import io.dinject.controller.QueryParam;

import java.util.List;


public class Simple$httpclient implements Simple {

  final HttpClientContext clientContext;

  public Simple$httpclient(HttpClientContext clientContext) {
    this.clientContext = clientContext;
  }

  //@Get("users/{user}/repos")
  @Override
  public List<Repo> listRepos(String user, @QueryParam("foo-too") String fooToo) {
    return clientContext.request()
      .path("users").path(user).path("repos").param("foo-too", fooToo)
      .get()
      .list(Repo.class);
  }

}
