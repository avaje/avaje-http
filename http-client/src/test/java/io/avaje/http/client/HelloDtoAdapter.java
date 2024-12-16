package io.avaje.http.client;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.json.node.JsonNodeMapper;
import io.avaje.json.node.JsonObject;
import org.example.webserver.HelloDto;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

final class HelloDtoAdapter implements JsonAdapter<HelloDto> {

  private final JsonNodeMapper nodeMapper;

  HelloDtoAdapter() {
    nodeMapper = JsonNodeMapper.builder().build();
  }

  @Override
  public void toJson(JsonWriter writer, HelloDto value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public HelloDto fromJson(JsonReader reader) {
    JsonObject jsonObject = nodeMapper.objectMapper().fromJson(reader);

    int id = jsonObject.extract("id", 0);
    String name = jsonObject.extract("name", "");
    String otherParam = jsonObject.extract("otherParam", "");

    var hello = new HelloDto(id, name, otherParam);
    UUID gid = jsonObject.extractOrEmpty("gid")
      .map(UUID::fromString)
      .orElse(null);
    hello.setGid(gid);

    String when = jsonObject.extract("whenAction", (String) null);
    if (when != null) {
      hello.setWhenAction(Instant.parse(when));
    }
    return hello;
  }
}
