package org.example;

import io.avaje.http.api.Default;
import io.avaje.http.api.Get;
import io.avaje.http.api.Path;
import io.avaje.http.api.Produces;
import io.avaje.http.api.QueryParam;

@Path("queryDefaults")
interface QueryDefaultsApi {

  @Produces("text/plain")
  @Get
  String defaults(
      @QueryParam("useMaster") @Default("false") boolean useMaster,
      @QueryParam("withFleets") @Default("false") boolean withFleets);
}
