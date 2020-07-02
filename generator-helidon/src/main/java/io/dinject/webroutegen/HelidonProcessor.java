package io.dinject.webroutegen;

import java.io.IOException;

public class HelidonProcessor extends BaseProcessor {

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new HelidonPlatformAdapter();
  }

  @Override
  void writeControllerAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException {
    new ControllerWriter(reader, ctx).write();
  }
}
