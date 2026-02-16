package io.avaje.http.maven.openapi.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.NoSuchElementException;

@CustomAdapter
public class SecuritySchemeInCustomAdaptor implements JsonAdapter<SecurityScheme.In> {

  private final JsonAdapter<String> stringJsonAdapter;

  public SecuritySchemeInCustomAdaptor(final Jsonb jsonb) {
    this.stringJsonAdapter = jsonb.adapter(String.class);
  }

  @Override
  public void toJson(JsonWriter writer, SecurityScheme.In value) {
    stringJsonAdapter.toJson(writer, value.toString());
  }

  @Override
  public SecurityScheme.In fromJson(JsonReader reader) {
    final String v = stringJsonAdapter.fromJson(reader);
    switch (v) {
      case "cookie":
      case "COOKIE":
        return SecurityScheme.In.COOKIE;
      case "header":
      case "HEADER":
        return SecurityScheme.In.HEADER;
      case "query":
      case "QUERY":
        return SecurityScheme.In.QUERY;
      default:
        throw new NoSuchElementException("No SecurityScheme.In matching " + v );
    }
  }
}
