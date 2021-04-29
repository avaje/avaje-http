package org.example;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;

import java.util.List;

@Client
@Path("users")
public interface GitHubUsers {

  @Get("{user}/repos")
  List<Repo> listRepos(String user);

  @Get("{user}/repos")
  String list2(String user);

}
