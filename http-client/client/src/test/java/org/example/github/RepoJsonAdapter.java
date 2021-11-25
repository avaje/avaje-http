package org.example.github;

import io.avaje.jsonb.JsonAdapter;
import io.avaje.jsonb.JsonReader;
import io.avaje.jsonb.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.spi.PropertyNames;
import io.avaje.jsonb.spi.ViewBuilder;
import io.avaje.jsonb.spi.ViewBuilderAware;

import java.lang.invoke.MethodHandle;

public class RepoJsonAdapter extends JsonAdapter<Repo> implements ViewBuilderAware {

  // naming convention Match
  // id [long] name:id publicField
  // name [java.lang.String] name:name publicField

  private final JsonAdapter<Long> plongJsonAdapter;
  private final JsonAdapter<String> stringJsonAdapter;
  private final PropertyNames names;

  public RepoJsonAdapter(Jsonb jsonb) {
    this.plongJsonAdapter = jsonb.adapter(Long.TYPE);
    this.stringJsonAdapter = jsonb.adapter(String.class);
    this.names = jsonb.properties("id", "name");
  }

  @Override
  public boolean isViewBuilderAware() {
    return true;
  }

  @Override
  public ViewBuilderAware viewBuild() {
    return this;
  }

  @Override
  public void build(ViewBuilder builder, String name, MethodHandle handle) {
    builder.beginObject(name, handle);
    builder.add("id", plongJsonAdapter, builder.field(Repo.class, "id"));
    builder.add("name", stringJsonAdapter, builder.field(Repo.class, "name"));
    builder.endObject();
  }

  @Override
  public void toJson(JsonWriter writer, Repo repo) {
    writer.beginObject();
    writer.names(names);
    writer.name(0);
    plongJsonAdapter.toJson(writer, repo.id);
    writer.name(1);
    stringJsonAdapter.toJson(writer, repo.name);
    writer.endObject();
  }

  @Override
  public Repo fromJson(JsonReader reader) {
    Repo _$repo = new Repo();

    // read json
    reader.beginObject();
    while (reader.hasNextField()) {
      String fieldName = reader.nextField();
      switch (fieldName) {
        case "id": {
          _$repo.id = plongJsonAdapter.fromJson(reader); break;
        }
        case "name": {
          _$repo.name = stringJsonAdapter.fromJson(reader); break;
        }
        default: {
          reader.unmappedField(fieldName);
          reader.skipValue();
        }
      }
    }
    reader.endObject();

    return _$repo;
  }
}
