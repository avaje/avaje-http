package org.example;

import io.avaje.http.api.Controller;

@Controller
class QueryDefaultsController implements QueryDefaultsApi {

  @Override
  public String defaults(boolean useMaster, boolean withFleets) {
    return useMaster + "," + withFleets;
  }
}
