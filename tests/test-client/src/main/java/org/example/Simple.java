package org.example;


import io.dinject.controller.Get;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Simple {


  @Get("users/{user}/repos")
  List<Repo> listRepos(String user, String other) throws IOException, InterruptedException;

  @Get("users/{id}")
  Repo getById(String id);
  //Call<List<Repo>> listRepos(@Path("user") String user);

  @Get("users/{id}")
  HttpResponse<Repo> getById2(String id);

  @Get("users/{id}")
  void readUsing(String id, Consumer<Stream<String>> consumer);

  @Get("dwn/{id}")
  void downloadMe(String id, Consumer<InputStream> file);

}
