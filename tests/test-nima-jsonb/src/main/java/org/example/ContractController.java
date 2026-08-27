package org.example;

import io.avaje.http.api.Controller;

@Controller
public class ContractController implements ContractApi {

  @Override
  public String getUser(String userId) {
    return "user:" + userId;
  }

  @Override
  public String getRepo(String repo, String org) {
    return "org:" + org + ",repo:" + repo;
  }

  @Override
  public String search(String q, String filter) {
    return "q:" + q + ",filter:" + filter;
  }

  @Override
  public String userRoles(String org, String user, Boolean activeOnly) {
    return "org:" + org + ",user:" + user + ",activeOnly:" + activeOnly;
  }
}
