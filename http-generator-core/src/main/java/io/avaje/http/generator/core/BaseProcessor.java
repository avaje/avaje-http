package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ProcessingContext.*;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedOptions({"useJavax", "useSingleton", "instrumentRequests","disableDirectWrites"})
public abstract class BaseProcessor extends AbstractProcessor {

  protected boolean useJsonB;

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
    ProcessingContext.init(processingEnv, providePlatformAdapter());
    useJsonB = ProcessingContext.useJsonb();
  }

  /** Provide the platform specific adapter to use for Javalin, Helidon etc. */
  protected abstract PlatformAdapter providePlatformAdapter();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {

    if (isOpenApiAvailable()) {
      readOpenApiDefinition(round);
      readTagDefinitions(round);
      readSecuritySchemes(round);
    }

    final Set<? extends Element> controllers =
        round.getElementsAnnotatedWith(typeElement(ControllerPrism.PRISM_TYPE));
    for (final Element controller : controllers) {
      writeAdapter(controller);
    }

    if (round.processingOver()) {
      writeOpenAPI();
    }
    return false;
  }

  private void readOpenApiDefinition(RoundEnvironment round) {
    final Set<? extends Element> elements =
        round.getElementsAnnotatedWith(typeElement(OpenAPIDefinitionPrism.PRISM_TYPE));
    for (final Element element : elements) {
      doc().readApiDefinition(element);
    }
  }

  private void readTagDefinitions(RoundEnvironment round) {
    Set<? extends Element> elements =
        round.getElementsAnnotatedWith(typeElement(TagPrism.PRISM_TYPE));
    for (final Element element : elements) {
      doc().addTagDefinition(element);
    }

    elements = round.getElementsAnnotatedWith(typeElement(TagsPrism.PRISM_TYPE));
    for (final Element element : elements) {
      doc().addTagsDefinition(element);
    }
  }

  private void readSecuritySchemes(RoundEnvironment round) {
    Set<? extends Element> elements = round.getElementsAnnotatedWith(typeElement(SecuritySchemePrism.PRISM_TYPE));
    for (final Element element : elements) {
        doc().addSecurityScheme(element);
    }

    elements = round.getElementsAnnotatedWith(typeElement(SecuritySchemesPrism.PRISM_TYPE));
    for (final Element element : elements) {
        doc().addSecuritySchemes(element);
    }
  }

  private void writeOpenAPI() {
    doc().writeApi();
  }

  private void writeAdapter(Element controller) {
    if (controller instanceof TypeElement) {
      final ControllerReader reader = new ControllerReader((TypeElement) controller);
      reader.read(true);
      try {
        writeControllerAdapter(reader);
      } catch (final Throwable e) {
        e.printStackTrace();
        logError(reader.beanType(), "Failed to write $Route class " + e);
      }
    }
  }

  /**
   * Write the adapter code for the given controller.
   */
  public abstract void writeControllerAdapter(ControllerReader reader) throws IOException;

}
