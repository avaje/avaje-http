package io.avaje.http.maven.openapi.jsonb;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.CustomAdapter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.Map;

@CustomAdapter
public final class ApiResponsesCustomAdaptor implements JsonAdapter<ApiResponses> {

  final JsonAdapter<Map<String, ApiResponse>> jsonAdapter;

  public ApiResponsesCustomAdaptor(final Jsonb jsonb) {
    jsonAdapter = jsonb.adapter(Types.newParameterizedType(Map.class, String.class, ApiResponse.class));
  }

  @Override
  public void toJson(JsonWriter writer, ApiResponses value) {
    jsonAdapter.toJson(writer, value);
  }

  @Override
  public ApiResponses fromJson(JsonReader reader) {
    final Map<String, ApiResponse> results = jsonAdapter.fromJson(reader);
    if (results == null) {
      return null;
    }
    final ApiResponses result = new ApiResponses();
    results.forEach(result::addApiResponse);
    return result;
  }
}
