package org.example;

import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.avaje.http.api.QueryParam;

@Path("/contract")
public interface ContractApi {

  @Produces("text/plain")
  @Get("users/{userId}")
  String getUser(String userId);

  @Produces("text/plain")
  @Get("repos/{org}/{repo}")
  String getRepo(String repo, String org);

  @Produces("text/plain")
  @Get("search")
  String search(@QueryParam("q") String q, @QueryParam String filter);

  @Produces("text/plain")
  @Get("orgs/{org}/users/{user}/roles")
  String userRoles(String org, String user, @QueryParam("activeOnly") Boolean activeOnly);
}
