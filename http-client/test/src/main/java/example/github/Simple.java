package example.github;

//import io.avaje.http.api.Get;
//import io.avaje.http.api.Path;
import io.avaje.http.client.HttpException;

import java.util.List;

//@Path("/")
public interface Simple {

  //@Get("users/{user}/repos")
  List<Repo> listRepos(String user, String other) throws HttpException;

}
