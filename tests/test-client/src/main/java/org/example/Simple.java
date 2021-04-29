package org.example;


import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.QueryParam;

import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Client
public interface Simple {

  // UUID goo, Boolean option,
  @Get("{uid}")
  HttpResponse<String> byId(long uid, LocalTime forT, @QueryParam("my-dat") LocalDate dt);

  @Get("users/{user}/repos")
  List<Repo> listRepos(String user, String other);

}
