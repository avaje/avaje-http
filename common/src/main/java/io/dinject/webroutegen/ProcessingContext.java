package io.dinject.webroutegen;

import io.dinject.webroutegen.openapi.DocContext;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;

public class ProcessingContext {

  private final Messager messager;
  private final Filer filer;
  private final Elements elements;
  private final Types types;
  private final String generatedAnnotation;
  private final boolean openApiAvailable;
  private final DocContext docContext;

  ProcessingContext(ProcessingEnvironment env) {
    this.messager = env.getMessager();
    this.filer = env.getFiler();
    this.elements = env.getElementUtils();
    this.types = env.getTypeUtils();
    this.openApiAvailable = isTypeAvailable(Constants.OPENAPIDEFINITION);
    this.docContext = new DocContext(env, openApiAvailable);
    boolean jdk8 = env.getSourceVersion().compareTo(SourceVersion.RELEASE_8) <= 0;
    this.generatedAnnotation = generatedAnnotation(jdk8);
  }

  private String generatedAnnotation(boolean jdk8) {
    if (jdk8) {
      return isTypeAvailable(Constants.GENERATED_8) ? Constants.GENERATED_8 : null;
    }
    return isTypeAvailable(Constants.GENERATED_9) ? Constants.GENERATED_9 : null;
  }

  private boolean isTypeAvailable(String canonicalName) {
    return null != getTypeElement(canonicalName);
  }

  TypeElement getTypeElement(String canonicalName) {
    return elements.getTypeElement(canonicalName);
  }

  boolean isGeneratedAvailable() {
    return generatedAnnotation != null;
  }

  String getGeneratedAnnotation() {
    return generatedAnnotation;
  }

  boolean isOpenApiAvailable() {
    return openApiAvailable;
  }

  void logError(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

//  void logDebug(String msg, Object... args) {
//    messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
//  }

  /**
   * Create a file writer for the given class name.
   */
  JavaFileObject createWriter(String cls, Element origin) throws IOException {
    return filer.createSourceFile(cls, origin);
  }

  String getDocComment(Element param) {
    return elements.getDocComment(param);
  }

  DocContext doc() {
    return docContext;
  }

  Element asElement(TypeMirror typeMirror) {
    return types.asElement(typeMirror);
  }

  TypeMirror asMemberOf(DeclaredType declaredType, Element element) {
    return types.asMemberOf(declaredType, element);
  }

}
