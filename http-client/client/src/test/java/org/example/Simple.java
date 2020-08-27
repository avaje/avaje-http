package org.example;

import io.avaje.http.client.HttpException;
import io.dinject.controller.Get;
import io.dinject.controller.Path;
import io.dinject.controller.Post;

import java.net.http.HttpResponse;
import java.util.List;

@Path("/foo")
public interface Simple {

  @Get("users/{user}/repos")
  List<Repo> listRepos(String user, String other) throws HttpException;

  @Get("users/{id}")
  Repo getById(String id) throws HttpException;

  @Get("users/{id}")
  HttpResponse<Repo> getById2(String id);

  @Post("users")
  Repo post(Repo repo) throws HttpException;

//  @Get("users/{id}")
//  void readUsing(String id, Consumer<Stream<String>> consumer);
//
//  @Get("dwn/{id}")
//  void downloadMe(String id, Consumer<InputStream> file);

}
