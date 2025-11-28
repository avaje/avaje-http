package io.avaje.http.generator.client;

import static io.avaje.http.generator.core.ProcessingContext.createMetaInfWriter;
import static io.avaje.http.generator.core.ProcessingContext.logError;
import static io.avaje.http.generator.core.ProcessingContext.platform;
import static io.avaje.http.generator.core.ProcessingContext.setPlatform;
import static io.avaje.http.generator.core.ProcessingContext.typeElement;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

import io.avaje.http.generator.core.APContext;
import io.avaje.http.generator.core.ClientPrism;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ImportPrism;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(io.avaje.http.api.Headers.class)
@SupportedAnnotationTypes({ClientPrism.PRISM_TYPE, ImportPrism.PRISM_TYPE})
public class ClientProcessor extends AbstractProcessor {

  private final ComponentMetaData metaData = new ComponentMetaData();
  private final Map<String, ComponentMetaData> privateMetaData = new HashMap<>();

  private SimpleComponentWriter componentWriter;
  private boolean readModuleInfo;
  private boolean generateComponent;
  private int rounds;

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
    if (generateComponent || round.errorRaised()) {
      return false;
    }
    generateComponent = rounds++ > 0;
    APContext.setProjectModuleElement(annotations, round);
    final var platform = platform();
    if (!(platform instanceof ClientPlatformAdapter)) {
      setPlatform(new ClientPlatformAdapter());
    }
    readModule();
    for (final Element controller : round.getElementsAnnotatedWith(typeElement(ClientPrism.PRISM_TYPE))) {
      if (ClientPrism.getInstanceOn(controller).generate()) {
        writeClient(controller);
        generateComponent = false;
      }
    }
    for (final var importedElement : round.getElementsAnnotatedWith(typeElement(ImportPrism.PRISM_TYPE))) {
      writeForImported(importedElement);
      generateComponent = false;
    }

    writeComponent(generateComponent);
    setPlatform(platform);
    return false;
  }

  /** Read the existing metadata from the generated component (if exists). */
  private void readModule() {
    if (readModuleInfo) {
      return;
    }
    readModuleInfo = true;
    new ComponentReader(metaData, privateMetaData).read();
  }

  private void writeForImported(Element importedElement) {
    ImportPrism.getInstanceOn(importedElement).value().stream()
      .map(ProcessingContext::asElement)
      .filter(Objects::nonNull)
      .forEach(this::writeClient);
  }

  private void writeClient(Element controller) {
    if (controller instanceof TypeElement) {
      final ControllerReader reader = new ControllerReader((TypeElement) controller);
      reader.read(false);
      try {
        var packagePrivate =
          !controller.getModifiers().contains(Modifier.PUBLIC)
            && ClientPrism.isPresent(controller);
        if (packagePrivate) {
          var packageName = APContext.elements().getPackageOf(controller).getQualifiedName().toString();
          var meta = privateMetaData.computeIfAbsent(packageName, k -> new ComponentMetaData());
          meta.add(writeClientAdapter(reader, true));
        } else {
          metaData.add(writeClientAdapter(reader, false));
        }

      } catch (final Exception e) {
        logError(reader.beanType(), "Failed to write client class " + e);
      }
    }
  }

  protected String writeClientAdapter(ControllerReader reader, boolean packagePrivate) throws IOException {
    var suffix = ClientSuffix.fromInterface(reader.beanType().getQualifiedName().toString());
    return new ClientWriter(reader, suffix, packagePrivate).write();
  }

  private void writeComponent(boolean processingOver) {
    if (processingOver) {
      try {
        if (!metaData.all().isEmpty()) {
          ProcessingContext.addClientComponent(metaData.fullName());
          componentWriter.init();
          componentWriter.write();
        }

        for (var meta : privateMetaData.values()) {
          ProcessingContext.addClientComponent(meta.fullName());
          var writer = new SimpleComponentWriter(meta);
          writer.init();
          writer.write();
        }
        writeMetaInf();
        ProcessingContext.validateModule();
      } catch (final IOException e) {
        logError("Error writing component", e);
      }
    }
  }

  void writeMetaInf() throws IOException {
    final FileObject fileObject = createMetaInfWriter(Constants.META_INF_COMPONENT);
    if (fileObject != null) {
      try (var fileWriter = fileObject.openWriter()) {
        if (!metaData.all().isEmpty()) {
          fileWriter.write(metaData.fullName());
          fileWriter.write("\n");
        }

        for (var meta : privateMetaData.values()) {
          fileWriter.write(meta.fullName());
          fileWriter.write("\n");
        }
      }
    }
  }
}
