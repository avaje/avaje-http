package io.avaje.http.maven.openapi.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import java.util.List;
import java.util.Map;

@CustomAdapter
public final class SecurityRequirementCustomAdaptor implements JsonAdapter<SecurityRequirement> {

  final JsonAdapter<Map<String, List<String>>> jsonAdapter;

  public SecurityRequirementCustomAdaptor(final Jsonb jsonb) {
    jsonAdapter = jsonb.adapter(Types.newParameterizedType(Map.class, String.class, Types.listOf(String.class)));
  }

  @Override
  public void toJson(JsonWriter writer, SecurityRequirement value) {
    jsonAdapter.toJson(writer, value);
  }

  @Override
  public SecurityRequirement fromJson(JsonReader reader) {
    final Map<String, List<String>> results = jsonAdapter.fromJson(reader);
    if (results == null) {
      return null;
    }
    final SecurityRequirement securityRequirement = new SecurityRequirement();
    results.forEach(securityRequirement::addList);
    return securityRequirement;
  }
}
