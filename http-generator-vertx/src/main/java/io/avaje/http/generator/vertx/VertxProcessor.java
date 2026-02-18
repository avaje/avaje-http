package io.avaje.http.generator.vertx;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import java.io.IOException;

public final class VertxProcessor extends BaseProcessor {

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new VertxAdapter();
  }

  @Override
  public void writeControllerAdapter(ControllerReader reader) throws IOException {
    new VertxControllerWriter(reader).write();
  }
}
