package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ProcessingContext.doc;
import static io.avaje.http.generator.core.ProcessingContext.elements;
import static io.avaje.http.generator.core.ProcessingContext.isOpenApiAvailable;
import static io.avaje.http.generator.core.ProcessingContext.logError;
import static io.avaje.http.generator.core.ProcessingContext.typeElement;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

@SupportedOptions({"useJavax", "useSingleton", "instrumentRequests","disableDirectWrites"})
public abstract class BaseProcessor extends AbstractProcessor {

  String contextPathString;

  Map<String, String> packagePaths= new HashMap<>();

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(PathPrism.PRISM_TYPE, ControllerPrism.PRISM_TYPE, OpenAPIDefinitionPrism.PRISM_TYPE);
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    ProcessingContext.init(processingEnv, providePlatformAdapter());
  }

  /** Provide the platform specific adapter to use for Javalin, Helidon etc. */
  protected abstract PlatformAdapter providePlatformAdapter();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {

    var pathElements = round.getElementsAnnotatedWith(typeElement(PathPrism.PRISM_TYPE));

    if (contextPathString == null) {
      contextPathString =
          ElementFilter.modulesIn(pathElements).stream()
              .map(PathPrism::getInstanceOn)
              .map(PathPrism::value)
              .findFirst()
              .orElse("");
    }
    packagePaths.putAll(
        ElementFilter.packagesIn(pathElements).stream()
            .collect(
                toMap(
                    p -> p.getQualifiedName().toString(),
                    p -> PathPrism.getInstanceOn(p).value())));

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

      var packageFQN = elements().getPackageOf(controller).getQualifiedName().toString();
      var contextPath =
          Util.combinePath(
              contextPathString,
              packagePaths.entrySet().stream()
                  .filter(k -> packageFQN.contains(k.getKey()))
                  .map(Entry::getValue)
                  .reduce(Util::combinePath)
                  .orElse(null));

      final ControllerReader reader = new ControllerReader((TypeElement) controller, contextPath);
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
