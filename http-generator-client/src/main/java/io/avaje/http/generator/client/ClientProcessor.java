package io.avaje.http.generator.client;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.avaje.http.api.Client;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.JsonBUtil;
import io.avaje.http.generator.core.ProcessingContext;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(Client.class)
public class ClientProcessor extends AbstractProcessor {

  private static final String METAINF_SERVICES_PROVIDER = "META-INF/services/io.avaje.http.client.HttpApiProvider";

  private final Set<String> generatedClients = new LinkedHashSet<>();

  protected ProcessingContext ctx;

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
  public Set<String> getSupportedAnnotationTypes() {
    final Set<String> annotations = new LinkedHashSet<>();
    annotations.add(Client.class.getCanonicalName());
    annotations.add(Client.Import.class.getCanonicalName());
    return annotations;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.processingEnv = processingEnv;
    this.ctx = new ProcessingContext(processingEnv, new ClientPlatformAdapter());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
    for (final Element controller : round.getElementsAnnotatedWith(Client.class)) {
      writeClient(controller);
    }
    for (final Element importedElement : round.getElementsAnnotatedWith(Client.Import.class)) {
      writeForImported(importedElement);
    }
    if (round.processingOver()) {
      writeServicesFile();
    }
    return false;
  }

  private void writeServicesFile() {
    try {
      final var metaInfWriter = ctx.createMetaInfWriter(METAINF_SERVICES_PROVIDER);
      final var writer = metaInfWriter.openWriter();
      for (final String generatedClient : generatedClients) {
        writer.append(generatedClient).append("$Provider\n");
      }
      writer.close();
    } catch (final IOException e) {
      ctx.logError(null, "Error writing services file " + e, e);
    }
  }

  private void writeForImported(Element importedElement) {
    for (final AnnotationMirror annotationMirror : importedElement.getAnnotationMirrors()) {
      for (final AnnotationValue value : annotationMirror.getElementValues().values()) {
        for (final Object apiClassDef : (List<?>) value.getValue()) {
          writeImported(apiClassDef.toString());
        }
      }
    }
  }

  private void writeImported(String fullName) {
    // trim .class suffix
    final var apiClassName = fullName.substring(0, fullName.length() - 6);
    final var typeElement = ctx.typeElement(apiClassName);
    if (typeElement != null) {
      writeClient(typeElement);
    }
  }

  private void writeClient(Element controller) {
    if (controller instanceof TypeElement) {
      final var reader = new ControllerReader((TypeElement) controller, ctx);
      reader.read(false);
      try {
        generatedClients.add(writeClientAdapter(ctx, reader));
      } catch (final Throwable e) {
        e.printStackTrace();
        ctx.logError(reader.beanType(), "Failed to write client class " + e);
      }
    }
  }

  protected String writeClientAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException {
    return new ClientWriter(reader, ctx, useJsonB).write();
  }

}
