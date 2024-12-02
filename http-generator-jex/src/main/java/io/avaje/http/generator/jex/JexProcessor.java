package io.avaje.http.generator.jex;

import io.avaje.http.generator.core.BaseProcessor;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.prism.AnnotationProcessor;

import java.io.IOException;

@AnnotationProcessor
public class JexProcessor extends BaseProcessor {

  @Override
  protected PlatformAdapter providePlatformAdapter() {
    return new JexAdapter();
  }

  @Override
  public void writeControllerAdapter(ControllerReader reader) throws IOException {
    new ControllerWriter(reader, ProcessingContext.useJsonb()).write();
  }
}
