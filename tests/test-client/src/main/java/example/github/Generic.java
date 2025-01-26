package example.github;

import java.util.List;

import io.avaje.http.api.Client;
import io.avaje.http.api.Post;
import io.avaje.http.client.HttpException;

@Client
public interface Generic {

  @Post("/generic")
  List<GenericData<Repo>> post(GenericData<Repo> repo) throws HttpException;

}
