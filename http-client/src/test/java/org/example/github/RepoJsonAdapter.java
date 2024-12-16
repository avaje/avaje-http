package org.example.github;

import io.avaje.json.JsonAdapter;
import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.Jsonb;
import io.avaje.json.PropertyNames;
import io.avaje.json.view.ViewBuilder;
import io.avaje.json.view.ViewBuilderAware;

import java.lang.invoke.MethodHandle;

public class RepoJsonAdapter implements JsonAdapter<Repo>, ViewBuilderAware {

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
    writer.beginObject(names);
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
    reader.beginObject(names);
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
