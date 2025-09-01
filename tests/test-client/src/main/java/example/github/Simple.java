package example.github;

import java.util.List;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.api.SuppressLogging;
import io.avaje.http.client.HttpException;

@Client
public interface Simple {

  @SuppressLogging
  @Get("users/{user}/repos")
  List<Repo> listRepos(String user, String other) throws HttpException;
}
