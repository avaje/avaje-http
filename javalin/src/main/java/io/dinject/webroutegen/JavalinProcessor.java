package io.dinject.webroutegen;

import java.io.IOException;

public class JavalinProcessor extends BaseProcessor {
  @Override
  void writeControllerAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException {
    ControllerRouteWriter writer = new ControllerRouteWriter(reader, ctx);
    writer.write();
  }
}
