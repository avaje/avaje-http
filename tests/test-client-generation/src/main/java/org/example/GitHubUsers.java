package org.example;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.client.HttpCall;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Client
@Path("users")
public interface GitHubUsers {

  @Get("{user}/repos")
  void plainVoid(String user);

  @Get("{user}/repos")
  HttpResponse<Void> asVoid(String user);

  @Get("{user}/repos")
  HttpCall<HttpResponse<Void>> callAsVoid(String user);

  @Get("{user}/repos")
  String plainString(String user);

  @Get("{user}/repos")
  HttpResponse<String> asStr(String user);

  @Get("{user}/repos")
  HttpCall<HttpResponse<String>> callAsStr(String user);

  @Get("{user}/repos")
  List<Repo> listRepos(String user);

  @Get("{user}/repos")
  HttpCall<List<Repo>> callListRepos(String user);

  @Get("{user}/repos/stream")
  Stream<Repo> streamRepos(String user);

  @Get("{user}/repos/stream")
  HttpCall<Stream<Repo>> callStreamRepos(String user);

  @Get("{user}/repos")
  HttpResponse<Path> withHan(String user, HttpResponse.BodyHandler<Path> myHandler);

  @Get("{user}/repos")
  HttpCall<HttpResponse<Path>> callWithHan(String user, HttpResponse.BodyHandler<Path> myHandler);

  @Get("{user}/repos/stream")
  Repo beanRepo(String user);

  @Get("{user}/repos/stream")
  HttpCall<Repo> callBeanRepo(String user);

//  @Get("{user}/repos/stream")
//  CompletableFuture<Void> asyncVoid(String user);

  @Get("{user}/repos/stream")
  CompletableFuture<HttpResponse<Void>> asyncVoid(String user);

  @Get("{user}/repos/stream")
  CompletableFuture<HttpResponse<String>> asyncString(String user);

  @Get("{user}/repos/stream")
  CompletableFuture<Stream<Repo>> asyncStreamRepo(String user);

  @Get("{user}/repos/stream")
  CompletableFuture<List<Repo>> asyncListRepo(String user);

  @Get("{user}/repos/stream")
  CompletableFuture<Repo> asyncBeanRepo(String user);

}
