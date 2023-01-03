package io.avaje.http.generator.helidon.nima;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.ProcessingContext;

import java.io.IOException;

public class NimaProcessor extends BaseProcessor {

  private final boolean jsonB;

  public NimaProcessor() {
    var jsonBOnClassPath = false;
    try {
      Class.forName("io.avaje.jsonb.Jsonb");
      jsonBOnClassPath = true;
    } catch (final ClassNotFoundException e) {
    }
    jsonB = jsonBOnClassPath;
  }

  public NimaProcessor(boolean useJsonb) {
    jsonB = useJsonb;
  }

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new NimaPlatformAdapter();
  }

  @Override
  public void writeControllerAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException {
    new ControllerWriter(reader, ctx, jsonB).write();
  }
}
