package io.dinject.javalin.generator;

import io.dinject.controller.Controller;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashSet;
import java.util.Set;

public class Processor extends AbstractProcessor {

  private ProcessingContext ctx;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_8;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {

    Set<String> annotations = new LinkedHashSet<>();
    annotations.add(Controller.class.getCanonicalName());
    annotations.add(OpenAPIDefinition.class.getCanonicalName());
    return annotations;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.ctx = new ProcessingContext(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {

    if (ctx.isOpenApiAvailable()) {
      readOpenApiDefinition(round);
    }

    Set<? extends Element> controllers = round.getElementsAnnotatedWith(Controller.class);
    for (Element controller : controllers) {
      writeControllerAdapter(controller);
    }

    if (round.processingOver()) {
      writeOpenAPI();
    }
    return false;
  }

  private void readOpenApiDefinition(RoundEnvironment round) {

    Set<? extends Element> elements = round.getElementsAnnotatedWith(OpenAPIDefinition.class);
    for (Element element : elements) {
      ctx.doc().readApiDefinition(element);
    }
  }

  private void writeOpenAPI() {
    ctx.doc().writeApi();
  }

  private void writeControllerAdapter(Element controller) {
    if (controller instanceof TypeElement) {
      ControllerReader reader = new ControllerReader((TypeElement) controller, ctx);
      reader.read();
      try {
        ControllerRouteWriter writer = new ControllerRouteWriter(reader, ctx);
        writer.write();
      } catch (Exception e) {
        e.printStackTrace();
        ctx.logError(reader.getBeanType(), "Failed to write $route class");
      }
    }
  }

}
