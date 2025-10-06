package io.avaje.http.generator.core;

import static io.avaje.http.generator.core.ProcessingContext.*;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import io.avaje.http.generator.core.TypeMap.CustomHandler;
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

  private static final String HTTP_CONTROLLERS_TXT = "testAPI/controllers.txt";
  protected String contextPathString;

  protected Map<String, String> packagePaths = new HashMap<>();

  private final Set<String> clientFQNs = new HashSet<>();

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(
      PathPrism.PRISM_TYPE,
      ControllerPrism.PRISM_TYPE,
      OpenAPIDefinitionPrism.PRISM_TYPE,
      MappedParamPrism.PRISM_TYPE,
      MapImportPrism.PRISM_TYPE);
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    APContext.init(processingEnv);
    ProcessingContext.init(processingEnv, providePlatformAdapter());

    try {
      var txtFilePath = APContext.getBuildResource(HTTP_CONTROLLERS_TXT);
      if (txtFilePath.toFile().exists()) {
        Files.lines(txtFilePath).forEach(clientFQNs::add);
      }
      if (APContext.isTestCompilation()) {
        for (var path : clientFQNs) {
          TestClientWriter.writeActual(path);
        }
      }
    } catch (IOException e) {
      // not worth failing over
      logWarn("Error reading test controllers %s", e);
    }
  }

  /** Provide the platform specific adapter to use for Javalin, Helidon etc. */
  protected abstract PlatformAdapter providePlatformAdapter();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
    if (round.errorRaised()) {
      return false;
    }

    for (final var type : ElementFilter.typesIn(getElements(round, MappedParamPrism.PRISM_TYPE))) {
      var prism = MappedParamPrism.getInstanceOn(type);
      registerParamMapping(type, prism.factoryMethod());
    }

    for (final var type : getElements(round, MapImportPrism.PRISM_TYPE)) {
      var prism = MapImportPrism.getInstanceOn(type);
      registerParamMapping(APContext.asTypeElement(prism.value()), prism.factoryMethod());
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

    for (final var controller : ElementFilter.typesIn(round.getElementsAnnotatedWith(typeElement(ControllerPrism.PRISM_TYPE)))) {
      writeAdapter(controller);
    }

    if (round.processingOver()) {
      writeOpenAPI();
      ProcessingContext.validateModule();

      if (!APContext.isTestCompilation()) {
        try {
          Files.write(
              APContext.getBuildResource(HTTP_CONTROLLERS_TXT),
              clientFQNs,
              StandardOpenOption.CREATE,
              StandardOpenOption.WRITE);
        } catch (IOException e) {
          // not worth failing over
        }
      }
    }
    return false;
  }

  private Set<? extends Element> getElements(RoundEnvironment round, String name) {
    return Optional.ofNullable(typeElement(name))
      .map(round::getElementsAnnotatedWith)
      .orElse(Set.of());
  }

  private final void registerParamMapping(final TypeElement type, String factoryMethod) {
    if (factoryMethod.isBlank()) {
      Util.stringConstructor(type)
        .ifPresentOrElse(
          c -> TypeMap.add(new CustomHandler(UType.parse(type.asType()), "")),
          () -> logError(type, "Missing constructor %s(String s)"));

    } else {
      ElementFilter.methodsIn(type.getEnclosedElements()).stream()
        .filter(m -> m.getSimpleName().contentEquals(factoryMethod)
              && m.getModifiers().contains(Modifier.STATIC)
              && Util.singleStringParam(m))
        .findAny()
        .ifPresentOrElse(
          c -> TypeMap.add(new CustomHandler(UType.parse(type.asType()), factoryMethod)),
          () -> logError(type, "Missing static factory method %s(String s)", factoryMethod));
    }
  }

  private void readOpenApiDefinition(RoundEnvironment round) {
    for (final Element element : round.getElementsAnnotatedWith(typeElement(OpenAPIDefinitionPrism.PRISM_TYPE))) {
      doc().readApiDefinition(element);
    }
  }

  private void readTagDefinitions(RoundEnvironment round) {
    for (final Element element : round.getElementsAnnotatedWith(typeElement(TagPrism.PRISM_TYPE))) {
      doc().addTagDefinition(element);
    }
    for (final Element element : round.getElementsAnnotatedWith(typeElement(TagsPrism.PRISM_TYPE))) {
      doc().addTagsDefinition(element);
    }
  }

  private void readSecuritySchemes(RoundEnvironment round) {
    for (final Element element : round.getElementsAnnotatedWith(typeElement(SecuritySchemePrism.PRISM_TYPE))) {
      doc().addSecurityScheme(element);
    }
    for (final Element element : round.getElementsAnnotatedWith(typeElement(SecuritySchemesPrism.PRISM_TYPE))) {
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
      writeClientAdapter(reader);

    } catch (final Throwable e) {
      logError(reader.beanType(), "Failed to write $Route class " + e);
    }
  }

  private void writeClientAdapter(ControllerReader reader) {
    try {
      if (reader.beanType().getInterfaces().isEmpty()
          && "java.lang.Object".equals(reader.beanType().getSuperclass().toString())
          && new TestClientWriter(reader).write()) {
        clientFQNs.add(reader.beanType().getQualifiedName().toString() + "TestAPI");
      }
    } catch (final IOException e) {
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
