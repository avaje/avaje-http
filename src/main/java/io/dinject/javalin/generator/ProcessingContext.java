package io.dinject.javalin.generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;

import static io.dinject.javalin.generator.Constants.GENERATED;

class ProcessingContext {

  private final Messager messager;
  private final Filer filer;
  private final Elements elementUtils;
  private final boolean generatedAvailable;

  ProcessingContext(ProcessingEnvironment env) {
    this.messager = env.getMessager();
    this.filer = env.getFiler();
    this.elementUtils = env.getElementUtils();
    this.generatedAvailable = isTypeAvailable(GENERATED);
  }

  private boolean isTypeAvailable(String canonicalName) {
    return null != getTypeElement(canonicalName);
  }

  TypeElement getTypeElement(String canonicalName) {
    return elementUtils.getTypeElement(canonicalName);
  }

  boolean isGeneratedAvailable() {
    return generatedAvailable;
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

  String docComment(Element param) {
    return elementUtils.getDocComment(param);
  }
}
