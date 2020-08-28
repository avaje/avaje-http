package io.avaje.http.generator.core;

import io.avaje.http.api.Controller;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class BaseProcessor extends AbstractProcessor {

  protected ProcessingContext ctx;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
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
    this.ctx = new ProcessingContext(processingEnv, providePlatformAdapter());
  }

  /**
   * Provide the platform specific adapter to use for Javalin, Helidon etc.
   */
  protected abstract PlatformAdapter providePlatformAdapter();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {

    if (ctx.isOpenApiAvailable()) {
      readOpenApiDefinition(round);
      readTagDefinitions(round);
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

  private void readTagDefinitions(RoundEnvironment round) {
    Set<? extends Element> elements = round.getElementsAnnotatedWith(Tag.class);
    for (Element element : elements) {
      ctx.doc().addTagDefinition(element);
    }

    elements = round.getElementsAnnotatedWith(Tags.class);
    for (Element element : elements) {
      ctx.doc().addTagsDefinition(element);
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
        writeControllerAdapter(ctx, reader);
      } catch (Throwable e) {
        e.printStackTrace();
        ctx.logError(reader.getBeanType(), "Failed to write $route class " + e);
      }
    }
  }

  /**
   * Write the adapter code for the given controller.
   */
  public abstract void writeControllerAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException;

}
