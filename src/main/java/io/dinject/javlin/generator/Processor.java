package io.dinject.javlin.generator;

import io.dinject.controller.Controller;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.Set;

public class Processor extends AbstractProcessor {

  private ProcessingContext processingContext;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_8;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {

    Set<String> annotations = new LinkedHashSet<>();
    annotations.add(Controller.class.getCanonicalName());
    return annotations;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.processingContext = new ProcessingContext(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    Set<? extends Element> controllers = roundEnv.getElementsAnnotatedWith(Controller.class);
    for (Element controller : controllers) {
      writeControllerAdapter(controller);
    }

    return false;
  }

  private void writeControllerAdapter(Element controller) {
    if (controller instanceof TypeElement) {
      TypeElement te = (TypeElement) controller;
      BeanReader reader = new BeanReader(te, processingContext);
      reader.read();
      try {
        ControllerRouteWriter writer = new ControllerRouteWriter(reader, processingContext);
        writer.write();
      } catch (Exception e) {
        e.printStackTrace();
        processingContext.logError(reader.getBeanType(), "Failed to write $route class");
      }
    }
  }

}
