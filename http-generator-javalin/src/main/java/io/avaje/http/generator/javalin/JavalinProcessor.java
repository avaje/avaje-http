package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.*;

import java.io.IOException;

public class JavalinProcessor extends BaseProcessor {

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new JavalinAdapter(useJsonB);
  }

  @Override
  public void writeControllerAdapter(ControllerReader reader) throws IOException {
    new ControllerWriter(reader, useJsonB).write();
  }
}
