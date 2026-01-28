package io.avaje.http.maven.openapi.jsonb;

import io.avaje.jsonb.Json;
import io.swagger.v3.oas.models.servers.ServerVariable;

import java.util.List;

@Json.MixIn(ServerVariable.class)
public abstract class ServerVariableMixin {
  @Json.Ignore
  private List<String> _enum = null;
  @Json.Ignore
  private String _default = null;
}
