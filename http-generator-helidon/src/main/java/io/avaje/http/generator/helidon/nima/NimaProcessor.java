package io.avaje.http.generator.helidon.nima;

import java.io.IOException;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.helidon.Filter;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(value = Filter.class, superClass = AbstractFilterPrism.class)
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
