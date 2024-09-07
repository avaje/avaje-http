package io.avaje.http.generator.sigma;

import java.io.IOException;

import javax.annotation.processing.Processor;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.spi.ServiceProvider;

@ServiceProvider(Processor.class)
public class SigmaProcessor extends BaseProcessor {

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new SigmaAdapter();
  }

  @Override
  public void writeControllerAdapter(ControllerReader reader) throws IOException {
    new ControllerWriter(reader).write();
  }
}
