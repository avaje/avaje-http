package io.avaje.http.generator.client;

import static io.avaje.http.generator.core.ProcessingContext.*;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

import io.avaje.http.generator.core.ClientPrism;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ImportPrism;
import io.avaje.http.generator.core.JsonBUtil;
import io.avaje.http.generator.core.ProcessingContext;

@SupportedAnnotationTypes({ClientPrism.PRISM_TYPE, ImportPrism.PRISM_TYPE})
public class ClientProcessor extends AbstractProcessor {

  private static final String METAINF_SERVICES_PROVIDER = "META-INF/services/io.avaje.http.client.HttpApiProvider";

  private final Set<String> generatedClients = new LinkedHashSet<>();

  private final boolean useJsonB;

  public ClientProcessor() {
    useJsonB = JsonBUtil.detectJsonb();
  }

  public ClientProcessor(boolean useJsonb) {
    useJsonB = useJsonb;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.processingEnv = processingEnv;
    ProcessingContext.init(processingEnv, new ClientPlatformAdapter(), false);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
    final var platform = platform();
    if (!(platform instanceof ClientPlatformAdapter)) {
      setPlatform(new ClientPlatformAdapter());
    }

    for (final Element controller : round.getElementsAnnotatedWith(typeElement(ClientPrism.PRISM_TYPE))) {
      writeClient(controller);
    }
    for (final Element importedElement : round.getElementsAnnotatedWith(typeElement(ImportPrism.PRISM_TYPE))) {
      writeForImported(importedElement);
    }
    if (round.processingOver()) {
      writeServicesFile();
    }
    setPlatform(platform);
    return false;
  }

  private void writeServicesFile() {
    try {
      final FileObject metaInfWriter = createMetaInfWriter(METAINF_SERVICES_PROVIDER);
      final Writer writer = metaInfWriter.openWriter();
      for (String generatedClient : generatedClients) {
        writer.append(generatedClient).append("$Provider\n");
      }
      writer.close();
    } catch (IOException e) {
      logError(null, "Error writing services file " + e, e);
    }
  }

  private void writeForImported(Element importedElement) {
    ImportPrism.getInstanceOn(importedElement).types().stream()
        .map(ProcessingContext::asElement)
        .filter(Objects::nonNull)
        .forEach(this::writeClient);
  }

  private void writeClient(Element controller) {
    if (controller instanceof TypeElement) {
      ControllerReader reader = new ControllerReader((TypeElement) controller);
      reader.read(false);
      try {
        generatedClients.add(writeClientAdapter(reader));
      } catch (Throwable e) {
        e.printStackTrace();
        logError(reader.beanType(), "Failed to write client class " + e);
      }
    }
  }

  protected String writeClientAdapter(ControllerReader reader) throws IOException {
    return new ClientWriter(reader, useJsonB).write();
  }

}
