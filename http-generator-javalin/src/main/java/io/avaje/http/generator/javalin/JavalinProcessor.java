package io.avaje.http.generator.javalin;

import java.io.IOException;

import io.avaje.http.api.javalin.After;
import io.avaje.http.api.javalin.Before;
import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(value = After.class, superClass = AbstractCustomMethodPrism.class)
@GeneratePrism(value = Before.class, superClass = AbstractCustomMethodPrism.class)
public class JavalinProcessor extends BaseProcessor {

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new JavalinAdapter();
  }

  @Override
  public void writeControllerAdapter(ControllerReader reader) throws IOException {
    new ControllerWriter(reader, ProcessingContext.useJsonb()).write();
  }
}
