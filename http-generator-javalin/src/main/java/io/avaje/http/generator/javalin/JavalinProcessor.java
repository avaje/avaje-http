package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.ProcessingContext;

import java.io.IOException;

public class JavalinProcessor extends BaseProcessor {

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new JavalinAdapter();
  }

  @Override
  public void writeControllerAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException {
    new ControllerWriter(reader, ctx).write();
  }
}
