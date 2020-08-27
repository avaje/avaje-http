package org.example;


import io.dinject.controller.Get;

import java.util.List;

public interface Simple {

  @Get("users/{user}/repos")
  List<Repo> listRepos(String user, String other);

}
