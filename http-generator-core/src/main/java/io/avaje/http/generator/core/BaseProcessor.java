package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ProcessingContext.doc;
import static io.avaje.http.generator.core.ProcessingContext.elements;
import static io.avaje.http.generator.core.ProcessingContext.isOpenApiAvailable;
import static io.avaje.http.generator.core.ProcessingContext.logError;
import static io.avaje.http.generator.core.ProcessingContext.typeElement;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
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

import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateModuleInfoReader;

@GenerateAPContext
@GenerateModuleInfoReader
@SupportedOptions({
  "useJavax",
  "useSingleton",
  "instrumentRequests",
  "disableDirectWrites",
  "disableJsonB"
})
public abstract class BaseProcessor extends AbstractProcessor {

  private static final String HTTP_CONTROLLERS_TXT = "controllers.txt";

  protected String contextPathString;

  protected Map<String, String> packagePaths = new HashMap<>();

  private final Set<String> controllerFQNs = new HashSet<>();

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(
        PathPrism.PRISM_TYPE, ControllerPrism.PRISM_TYPE, OpenAPIDefinitionPrism.PRISM_TYPE);
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    APContext.init(processingEnv);
    ProcessingContext.init(processingEnv, providePlatformAdapter());

    try {
      var txtFilePath = APContext.getBuildResource(HTTP_CONTROLLERS_TXT);

      if (txtFilePath.toFile().exists()) {
        Files.lines(txtFilePath).forEach(controllerFQNs::add);
      }
      if (APContext.isTestCompilation()) {
        System.out.println("Trying to read: " + controllerFQNs + "from path:" + txtFilePath);
        controllerFQNs.stream().map(APContext::typeElement).forEach(this::writeClientAdapter);
      }
    } catch (IOException e) {
      e.printStackTrace();
      // not worth failing over
    }
  }

  /** Provide the platform specific adapter to use for Javalin, Helidon etc. */
  protected abstract PlatformAdapter providePlatformAdapter();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
    if (round.errorRaised()) {
      return false;
    }
    var pathElements = round.getElementsAnnotatedWith(typeElement(PathPrism.PRISM_TYPE));
    APContext.setProjectModuleElement(annotations, round);
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

    for (final var controller :
        ElementFilter.typesIn(
            round.getElementsAnnotatedWith(typeElement(ControllerPrism.PRISM_TYPE)))) {
      writeAdapter(controller);
    }

    if (round.processingOver()) {
      writeOpenAPI();
      ProcessingContext.validateModule();

      if (!APContext.isTestCompilation()) {
        try {
          Files.write(
              APContext.getBuildResource(HTTP_CONTROLLERS_TXT),
              controllerFQNs,
              StandardOpenOption.CREATE,
              StandardOpenOption.WRITE);
        } catch (IOException e) {
          // not worth failing over
        }
      }
    }
    return false;
  }

  private void readOpenApiDefinition(RoundEnvironment round) {
    for (final Element element :
        round.getElementsAnnotatedWith(typeElement(OpenAPIDefinitionPrism.PRISM_TYPE))) {
      doc().readApiDefinition(element);
    }
  }

  private void readTagDefinitions(RoundEnvironment round) {
    for (final Element element : round.getElementsAnnotatedWith(typeElement(TagPrism.PRISM_TYPE))) {
      doc().addTagDefinition(element);
    }
    for (final Element element :
        round.getElementsAnnotatedWith(typeElement(TagsPrism.PRISM_TYPE))) {
      doc().addTagsDefinition(element);
    }
  }

  private void readSecuritySchemes(RoundEnvironment round) {
    for (final Element element :
        round.getElementsAnnotatedWith(typeElement(SecuritySchemePrism.PRISM_TYPE))) {
      doc().addSecurityScheme(element);
    }
    for (final Element element :
        round.getElementsAnnotatedWith(typeElement(SecuritySchemesPrism.PRISM_TYPE))) {
      doc().addSecuritySchemes(element);
    }
  }

  private void writeOpenAPI() {
    doc().writeApi();
  }

  private void writeAdapter(TypeElement controller) {
    final var packageFQN = elements().getPackageOf(controller).getQualifiedName().toString();
    final var contextPath = Util.combinePath(contextPathString, packagePath(packageFQN));
    final var reader = new ControllerReader(controller, contextPath);
    reader.read(true);
    try {

      writeControllerAdapter(reader);
      controllerFQNs.add(controller.getQualifiedName().toString());

    } catch (final Throwable e) {
      logError(reader.beanType(), "Failed to write $Route class " + e);
    }
  }

  private void writeClientAdapter(TypeElement controller) {
    final var packageFQN = elements().getPackageOf(controller).getQualifiedName().toString();
    final var contextPath = Util.combinePath(contextPathString, packagePath(packageFQN));
    final var reader = new ControllerReader(controller, contextPath);
    reader.read(true);
    try {
      if (controller.getInterfaces().isEmpty()
          && "java.lang.Object".equals(controller.getSuperclass().toString())) {
        new TestClientWriter(reader).write();
      }
      writeControllerAdapter(reader);

    } catch (final Throwable e) {
      logError(reader.beanType(), "Failed to write $Route class " + e);
    }
  }

  private String packagePath(String packageFQN) {
    return packagePaths.entrySet().stream()
        .filter(k -> packageFQN.startsWith(k.getKey()))
        .map(Entry::getValue)
        .reduce(Util::combinePath)
        .orElse(null);
  }

  /** Write the adapter code for the given controller. */
  public abstract void writeControllerAdapter(ControllerReader reader) throws IOException;
}
