package org.example;

import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;
import java.util.List;

public class GitHubServiceTest {

  @Test
  public void test() throws IOException {

    Retrofit retrofit = new Retrofit.Builder()
      .baseUrl("https://api.github.com/")
      .addConverterFactory(ScalarsConverterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .build();

    GitHubService service = retrofit.create(GitHubService.class);
    final Call<List<Repo>> call = service.listRepos("octocat");
    final Response<List<Repo>> res = call.execute();
    final List<Repo> body = res.body();

    System.out.println("done "+body);

    final Call<String> call2 = service.list2("octocat");
    final Response<String> res2 = call2.execute();
    final String body2 = res2.body();

    System.out.println("done "+body2);

  }
}
