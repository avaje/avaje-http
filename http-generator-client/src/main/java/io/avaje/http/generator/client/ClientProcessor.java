package io.avaje.http.generator.client;

import io.avaje.http.api.Client;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ProcessingContext;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClientProcessor extends AbstractProcessor {

  private static final String METAINF_SERVICES_PROVIDER = "META-INF/services/io.avaje.http.client.HttpApiProvider";

  private final Set<String> generatedClients = new LinkedHashSet<>();

  protected ProcessingContext ctx;

  private boolean useJsonB;

  public ClientProcessor() {
    try {
      Class.forName("io.avaje.jsonb.Jsonb");
      this.useJsonB = true;
    } catch (final ClassNotFoundException e) {
      this.useJsonB = false;
    }
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
    Set<String> annotations = new LinkedHashSet<>();
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
    for (Element controller : round.getElementsAnnotatedWith(Client.class)) {
      writeClient(controller);
    }
    for (Element importedElement : round.getElementsAnnotatedWith(Client.Import.class)) {
      writeForImported(importedElement);
    }
    if (round.processingOver()) {
      writeServicesFile();
    }
    return false;
  }

  private void writeServicesFile() {
    try {
      final FileObject metaInfWriter = ctx.createMetaInfWriter(METAINF_SERVICES_PROVIDER);
      final Writer writer = metaInfWriter.openWriter();
      for (String generatedClient : generatedClients) {
        writer.append(generatedClient).append("$Provider\n");
      }
      writer.close();
    } catch (IOException e) {
      ctx.logError(null, "Error writing services file " + e, e);
    }
  }

  private void writeForImported(Element importedElement) {
    for (AnnotationMirror annotationMirror : importedElement.getAnnotationMirrors()) {
      for (AnnotationValue value : annotationMirror.getElementValues().values()) {
        for (Object apiClassDef : (List<?>) value.getValue()) {
          writeImported(apiClassDef.toString());
        }
      }
    }
  }

  private void writeImported(String fullName) {
    // trim .class suffix
    String apiClassName = fullName.substring(0, fullName.length() - 6);
    TypeElement typeElement = ctx.typeElement(apiClassName);
    if (typeElement != null) {
      writeClient(typeElement);
    }
  }

  private void writeClient(Element controller) {
    if (controller instanceof TypeElement) {
      ControllerReader reader = new ControllerReader((TypeElement) controller, ctx);
      reader.read(false);
      try {
        generatedClients.add(writeClientAdapter(ctx, reader));
      } catch (Throwable e) {
        e.printStackTrace();
        ctx.logError(reader.beanType(), "Failed to write client class " + e);
      }
    }
  }

  protected String writeClientAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException {
    return new ClientWriter(reader, ctx,useJsonB).write();
  }

}
