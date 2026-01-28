package io.avaje.http.maven.openapi.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;

import java.util.Map;

@CustomAdapter
public final class ContentCustomAdaptor implements JsonAdapter<Content> {

  final JsonAdapter<Map<String, MediaType>> jsonAdapter;

  public ContentCustomAdaptor(final Jsonb jsonb) {
    jsonAdapter = jsonb.adapter(Types.newParameterizedType(Map.class, String.class, MediaType.class));
  }

  @Override
  public void toJson(JsonWriter writer, Content value) {
    jsonAdapter.toJson(writer, value);
  }

  @Override
  public Content fromJson(JsonReader reader) {
    final Map<String, MediaType> results = jsonAdapter.fromJson(reader);
    if (results == null) {
      return null;
    }
    final Content content = new Content();
    results.forEach(content::addMediaType);
    return content;
  }
}
