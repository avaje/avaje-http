package io.avaje.http.generator.helidon;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;

import java.io.IOException; 

public class HelidonProcessor extends BaseProcessor {

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new HelidonPlatformAdapter();
  }

  @Override
  public void writeControllerAdapter(ControllerReader reader) throws IOException {
    new ControllerWriter(reader).write();
  }
}
