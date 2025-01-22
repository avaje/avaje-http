package io.avaje.http.generator.client;

import static io.avaje.http.generator.core.ProcessingContext.logError;
import static io.avaje.http.generator.core.ProcessingContext.platform;
import static io.avaje.http.generator.core.ProcessingContext.setPlatform;
import static io.avaje.http.generator.core.ProcessingContext.typeElement;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.avaje.http.generator.core.APContext;
import io.avaje.http.generator.core.ClientPrism;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ImportPrism;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(io.avaje.http.api.Headers.class)
@SupportedAnnotationTypes({ClientPrism.PRISM_TYPE, ImportPrism.PRISM_TYPE})
public class ClientProcessor extends AbstractProcessor {

  private final ComponentMetaData metaData = new ComponentMetaData();

  private SimpleComponentWriter componentWriter;

  private boolean readModuleInfo;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.processingEnv = processingEnv;
    APContext.init(processingEnv);
    ProcessingContext.init(processingEnv, new ClientPlatformAdapter(), false);
    this.componentWriter = new SimpleComponentWriter(metaData);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
    APContext.setProjectModuleElement(annotations, round);
    final var platform = platform();
    if (!(platform instanceof ClientPlatformAdapter)) {
      setPlatform(new ClientPlatformAdapter());
    }
    readModule();
    for (final Element controller : round.getElementsAnnotatedWith(typeElement(ClientPrism.PRISM_TYPE))) {
      if (ClientPrism.getInstanceOn(controller).generate()) {
        writeClient(controller);
      }
    }
    for (final var importedElement : round.getElementsAnnotatedWith(typeElement(ImportPrism.PRISM_TYPE))) {
      writeForImported(importedElement);
    }

    writeComponent(round.processingOver());
    setPlatform(platform);
    return false;
  }

  /** Read the existing metadata from the generated component (if exists). */
  private void readModule() {
    if (readModuleInfo) {
      return;
    }
    readModuleInfo = true;
    new ComponentReader(metaData).read();
  }

  private void writeForImported(Element importedElement) {
    ImportPrism.getInstanceOn(importedElement).types().stream()
      .map(ProcessingContext::asElement)
      .filter(Objects::nonNull)
      .forEach(this::writeClient);
  }

  private void writeClient(Element controller) {
    if (controller instanceof TypeElement) {
      final ControllerReader reader = new ControllerReader((TypeElement) controller);
      reader.read(false);
      try {
        metaData.add(writeClientAdapter(reader));
      } catch (final Exception e) {
        logError(reader.beanType(), "Failed to write client class " + e);
      }
    }
  }

  protected String writeClientAdapter(ControllerReader reader) throws IOException {
    var suffix = ClientSuffix.fromInterface(reader.beanType().getQualifiedName().toString());
    return new ClientWriter(reader, suffix).write();
  }

  private void initialiseComponent() {
    metaData.initialiseFullName();
    if (!metaData.all().isEmpty()) {
      ProcessingContext.addClientComponent(metaData.fullName());
      ProcessingContext.validateModule();
    }
    try {
      componentWriter.init();
    } catch (final IOException e) {
      logError("Error creating writer for JsonbComponent", e);
    }
  }

  private void writeComponent(boolean processingOver) {
    initialiseComponent();
    if (processingOver) {
      try {
        componentWriter.write();
      } catch (final IOException e) {
        logError("Error writing component", e);
      }
    }
  }
}
