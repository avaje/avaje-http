package io.avaje.http.generator.javalin;

import io.avaje.http.generator.core.*;
import io.avaje.http.javalin.After;
import io.avaje.http.javalin.Before;
import io.avaje.prism.GeneratePrism;

import java.io.IOException;

@GeneratePrism(Before.class)
@GeneratePrism(After.class)
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
