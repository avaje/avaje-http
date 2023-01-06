package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.ProcessingContext;

import java.io.IOException;

public class JavalinProcessor extends BaseProcessor {

  private final boolean useJsonB;

  public JavalinProcessor() {
    var jsonBOnClassPath = false;
    try {
      Class.forName("io.avaje.jsonb.Jsonb");
      jsonBOnClassPath = true;
    } catch (final ClassNotFoundException e) {
      // intentionally ignored
    }
    useJsonB = jsonBOnClassPath;
  }

  public JavalinProcessor(boolean useJsonb) {
    useJsonB = useJsonb;
  }

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new JavalinAdapter(useJsonB);
  }

  @Override
  public void writeControllerAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException {
    new ControllerWriter(reader, ctx, useJsonB).write();
  }
}
