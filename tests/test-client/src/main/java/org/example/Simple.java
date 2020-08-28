package org.example;


import io.avaje.http.api.Get;

import java.util.List;

public interface Simple {

  @Get("users/{user}/repos")
  List<Repo> listRepos(String user, String other);

}
