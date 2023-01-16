package org.example.github;

import io.avaje.http.client.HttpException;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Post;

import java.net.http.HttpResponse;
import java.util.List;

@Path("/")
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
