package io.avaje.http.generator.helidon.nima;

import java.io.IOException;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.ProcessingContext;

public class NimaProcessor extends BaseProcessor {

  boolean jsonB;

  public NimaProcessor() {
    try {
      Class.forName("io.avaje.jsonb.Jsonb");
      jsonB = true;
    } catch (final ClassNotFoundException e) {
      jsonB = false;
    }
  }

  public NimaProcessor(boolean useJsonb) {
    jsonB = useJsonb;
  }

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new NimaPlatformAdapter();
  }

  @Override
  public void writeControllerAdapter(ProcessingContext ctx, ControllerReader reader)
      throws IOException {

    new ControllerWriter(reader, ctx, jsonB).write();
  }
}
