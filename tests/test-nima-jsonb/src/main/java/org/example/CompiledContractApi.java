package org.example;

import io.avaje.http.api.Path;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;

@Path("/compiled-contract")
public interface CompiledContractApi {

  @Produces("application/json")
  @Post("people/echo")
  Person echo(Person request);
}
