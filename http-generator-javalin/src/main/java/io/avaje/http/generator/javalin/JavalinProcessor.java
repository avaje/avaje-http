package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.*;

import java.io.IOException;

public class JavalinProcessor extends BaseProcessor {

  private final boolean useJsonB;

  public JavalinProcessor() {
    useJsonB = JsonBUtil.detectJsonb();
  }

  public JavalinProcessor(boolean useJsonb) {
    useJsonB = useJsonb;
  }

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new JavalinAdapter(useJsonB);
  }

  @Override
  public void writeControllerAdapter(ControllerReader reader) throws IOException {
    new ControllerWriter(reader, useJsonB).write();
  }
}
