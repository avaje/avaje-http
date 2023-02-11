package io.avaje.http.generator.core;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedOptions({"useJavax", "useSingleton"})
public abstract class BaseProcessor extends AbstractProcessor {

  protected ProcessingContext ctx;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(ControllerPrism.PRISM_TYPE, OpenAPIDefinitionPrism.PRISM_TYPE);
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.ctx = new ProcessingContext(processingEnv, providePlatformAdapter());
  }

  /** Provide the platform specific adapter to use for Javalin, Helidon etc. */
  protected abstract PlatformAdapter providePlatformAdapter();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {

    if (ctx.isOpenApiAvailable()) {
      readOpenApiDefinition(round);
      readTagDefinitions(round);
    }

    final Set<? extends Element> controllers =
        round.getElementsAnnotatedWith(ctx.typeElement(ControllerPrism.PRISM_TYPE));
    for (Element controller : controllers) {
      writeControllerAdapter(controller);
    }

    if (round.processingOver()) {
      writeOpenAPI();
    }
    return false;
  }

  private void readOpenApiDefinition(RoundEnvironment round) {
    final Set<? extends Element> elements =
        round.getElementsAnnotatedWith(ctx.typeElement(OpenAPIDefinitionPrism.PRISM_TYPE));
    for (Element element : elements) {
      ctx.doc().readApiDefinition(element);
    }
  }

  private void readTagDefinitions(RoundEnvironment round) {
    Set<? extends Element> elements =
        round.getElementsAnnotatedWith(ctx.typeElement(TagPrism.PRISM_TYPE));
    for (Element element : elements) {
      ctx.doc().addTagDefinition(element);
    }

    elements = round.getElementsAnnotatedWith(ctx.typeElement(TagsPrism.PRISM_TYPE));
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
      reader.read(true);
      try {
        writeControllerAdapter(ctx, reader);
      } catch (Throwable e) {
        e.printStackTrace();
        ctx.logError(reader.beanType(), "Failed to write $Route class " + e);
      }
    }
  }

  /**
   * Write the adapter code for the given controller.
   */
  public abstract void writeControllerAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException;

}
