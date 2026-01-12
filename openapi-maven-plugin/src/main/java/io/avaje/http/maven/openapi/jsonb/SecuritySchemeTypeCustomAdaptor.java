package io.avaje.http.maven.openapi.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.NoSuchElementException;

@CustomAdapter
public class SecuritySchemeTypeCustomAdaptor implements JsonAdapter<SecurityScheme.Type> {

  private final JsonAdapter<String> stringJsonAdapter;

  public SecuritySchemeTypeCustomAdaptor(final Jsonb jsonb) {
    this.stringJsonAdapter = jsonb.adapter(String.class);
  }

  @Override
  public void toJson(JsonWriter writer, SecurityScheme.Type value) {
    stringJsonAdapter.toJson(writer, value.toString());
  }

  @Override
  public SecurityScheme.Type fromJson(JsonReader reader) {
    final String v = stringJsonAdapter.fromJson(reader);
    switch (v) {
      case "apiKey":
      case "APIKEY":
        return SecurityScheme.Type.APIKEY;
      case "http":
      case "HTTP":
        return SecurityScheme.Type.HTTP;
      case "oauth2":
      case "OAUTH2":
        return SecurityScheme.Type.OAUTH2;
      case "openIdConnect":
      case "OPENIDCONNECT":
        return SecurityScheme.Type.OPENIDCONNECT;
      case "mutualTLS":
      case "MUTUALTLS":
        return SecurityScheme.Type.MUTUALTLS;
      default:
        throw new NoSuchElementException("No SecurityScheme.Type matching " + v );
    }
  }
}
