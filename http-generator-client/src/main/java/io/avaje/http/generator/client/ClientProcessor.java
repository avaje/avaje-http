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
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClientProcessor extends AbstractProcessor {

  protected ProcessingContext ctx;

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
    return false;
  }

  private void writeForImported(Element importedElement) {
    for (AnnotationMirror annotationMirror : importedElement.getAnnotationMirrors()) {
      for (AnnotationValue value : annotationMirror.getElementValues().values()) {
        for (Object apiClassDef : (List<?>) value.getValue()) {
          String fullName = apiClassDef.toString();
          writeImported(fullName);
        }
      }
    }
  }

  private void writeImported(String fullName) {
    // trim .class suffix
    String apiClassName = fullName.substring(0, fullName.length() - 6);
    //ctx.logError(null, "build import:" + apiClassName);
    TypeElement typeElement = ctx.getTypeElement(apiClassName);
    if (typeElement != null) {
      writeClient(typeElement);
    }
  }

  private void writeClient(Element controller) {
    if (controller instanceof TypeElement) {
      ControllerReader reader = new ControllerReader((TypeElement) controller, ctx);
      reader.read(false);
      try {
        writeClientAdapter(ctx, reader);
      } catch (Throwable e) {
        e.printStackTrace();
        ctx.logError(reader.getBeanType(), "Failed to write client class " + e);
      }
    }
  }

  protected void writeClientAdapter(ProcessingContext ctx, ControllerReader reader) throws IOException {
    new ClientWriter(reader, ctx).write();
  }

}
