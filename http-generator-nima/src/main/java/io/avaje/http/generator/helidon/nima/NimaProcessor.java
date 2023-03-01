package io.avaje.http.generator.helidon.nima;

import io.avaje.http.generator.core.*;

import java.io.IOException;

public class NimaProcessor extends BaseProcessor {

  private final boolean jsonB;

  public NimaProcessor() {
    jsonB = JsonBUtil.detectJsonb();
  }

  public NimaProcessor(boolean useJsonb) {
    jsonB = useJsonb;
  }

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new NimaPlatformAdapter();
  }

  @Override
  public void writeControllerAdapter(ControllerReader reader) throws IOException {
    new ControllerWriter(reader, jsonB).write();
  }
}
