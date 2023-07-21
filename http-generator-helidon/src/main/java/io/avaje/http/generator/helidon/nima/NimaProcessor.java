package io.avaje.http.generator.helidon.nima;

import io.avaje.http.generator.core.*;

import java.io.IOException;

public class NimaProcessor extends BaseProcessor {

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new NimaPlatformAdapter();
  }

  @Override
  public void writeControllerAdapter(ControllerReader reader) throws IOException {
    new ControllerWriter(reader, useJsonB).write();
  }
}
