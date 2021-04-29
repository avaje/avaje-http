package org.example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

/**
 * Example Retrofit API.
 */
public interface GitHubService {

  @GET("users/{user}/repos")
  Call<List<Repo>> listRepos(@Path("user") String user);

  @GET("users/{user}/repos")
  Call<String> list2(@Path("user") String user);

}
