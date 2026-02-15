package io.avaje.http.maven.openapi.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

import java.util.Map;

@CustomAdapter
public final class ServerVariablesCustomAdaptor implements JsonAdapter<ServerVariables> {

  final JsonAdapter<Map<String, ServerVariable>> jsonAdapter;

  public ServerVariablesCustomAdaptor(final Jsonb jsonb) {
    jsonAdapter = jsonb.adapter(Types.newParameterizedType(Map.class, String.class, ServerVariable.class));
  }

  @Override
  public void toJson(JsonWriter writer, ServerVariables value) {
    jsonAdapter.toJson(writer, value);
  }

  @Override
  public ServerVariables fromJson(JsonReader reader) {
    final Map<String, ServerVariable> results = jsonAdapter.fromJson(reader);
    if (results == null) {
      return null;
    }
    final ServerVariables result = new ServerVariables();
    results.forEach(result::addServerVariable);
    return result;
  }
}
