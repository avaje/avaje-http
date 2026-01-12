package io.avaje.http.maven.openapi.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

import java.util.Map;

@CustomAdapter
public final class PathsCustomAdaptor implements JsonAdapter<Paths> {

  final JsonAdapter<Map<String, PathItem>> jsonAdapter;

  public PathsCustomAdaptor(final Jsonb jsonb) {
    jsonAdapter = jsonb.adapter(Types.newParameterizedType(Map.class, String.class, PathItem.class));
  }

  @Override
  public void toJson(JsonWriter writer, Paths value) {
    jsonAdapter.toJson(writer, value);
  }

  @Override
  public Paths fromJson(JsonReader reader) {
    final Map<String, PathItem> results = jsonAdapter.fromJson(reader);
    if (results == null) {
      return null;
    }
    final Paths result = new Paths();
    results.forEach(result::addPathItem);
    return result;
  }
}
