package io.dinject.javalin.generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;

class ProcessingContext {

  private final Messager messager;
  private final Filer filer;

  ProcessingContext(ProcessingEnvironment env) {
    this.messager = env.getMessager();
    this.filer = env.getFiler();
  }

  void logError(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  /**
   * Create a file writer for the given class name.
   */
  JavaFileObject createWriter(String cls, Element origin) throws IOException {
    return filer.createSourceFile(cls, origin);
  }
}
