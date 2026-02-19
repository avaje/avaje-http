package io.avaje.http.generator.vertx;

import java.io.IOException;

import io.avaje.http.api.vertx.Blocking;
import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(Blocking.class)
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
