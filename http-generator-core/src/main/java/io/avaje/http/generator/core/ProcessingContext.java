package io.avaje.http.generator.core;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import io.avaje.http.generator.core.openapi.DocContext;

public class ProcessingContext {

  private static PlatformAdapter readAdapter;
  private static Messager messager;
  private static Filer filer;
  private static Elements elements;
  private static Types types;
  private static boolean openApiAvailable;
  private static DocContext docContext;
  private static boolean useComponent;
  private static boolean useJavax;
  private static String diAnnotation;

  public static void init(ProcessingEnvironment env, PlatformAdapter adapter) {
    init(env, adapter, true);
  }

  public static void init(ProcessingEnvironment env, PlatformAdapter adapter, boolean generateOpenAPI) {
    readAdapter = adapter;
    messager = env.getMessager();
    filer = env.getFiler();
    elements = env.getElementUtils();
    types = env.getTypeUtils();

    if (generateOpenAPI) {
      openApiAvailable = isTypeAvailable(Constants.OPENAPIDEFINITION);
      docContext = new DocContext(env, openApiAvailable);
    }

    final var options = env.getOptions();
    final var singletonOverride = options.get("useSingleton");
    if (singletonOverride != null) {
      useComponent = !Boolean.parseBoolean(singletonOverride);
    } else {
      useComponent = isTypeAvailable(Constants.COMPONENT);
    }
    diAnnotation = useComponent ? "@Component" : "@Singleton";

    final var javax = isTypeAvailable(Constants.SINGLETON_JAVAX);
    final var jakarta = isTypeAvailable(Constants.SINGLETON_JAKARTA);
    final var override = env.getOptions().get("useJavax");
    if (override != null || (javax && jakarta)) {
      useJavax = Boolean.parseBoolean(override);
    } else {
      useJavax = javax;
    }
  }

  private static boolean isTypeAvailable(String canonicalName) {
    return null != typeElement(canonicalName);
  }

  public static TypeElement typeElement(String canonicalName) {
    return elements.getTypeElement(canonicalName);
  }

  public static boolean isOpenApiAvailable() {
    return openApiAvailable;
  }

  public static boolean useJavax() {
    return useJavax;
  }

  public static boolean useComponent() {
    return useComponent;
  }

  public static void logError(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  /**
   * Create a file writer for the given class name.
   */
  public static JavaFileObject createWriter(String cls, Element origin) throws IOException {
    return filer.createSourceFile(cls, origin);
  }

  /**
   * Create a file writer for the META-INF services file.
   */
  public static FileObject createMetaInfWriter(String target) throws IOException {
    return filer.createResource(StandardLocation.CLASS_OUTPUT, "", target);
  }

  public static String docComment(Element param) {
    return elements.getDocComment(param);
  }

  public static DocContext doc() {
    return docContext;
  }

  public static Element asElement(TypeMirror typeMirror) {
    return types.asElement(typeMirror);
  }

  public static TypeMirror asMemberOf(DeclaredType declaredType, Element element) {
    return types.asMemberOf(declaredType, element);
  }

  public static List<ExecutableElement> superMethods(Element element, String methodName) {
    return types.directSupertypes(element.asType()).stream()
      .filter(type -> !type.toString().contains("java.lang.Object"))
      .map(
        superType -> {
          final var superClass = (TypeElement) types.asElement(superType);
          for (final ExecutableElement method : ElementFilter.methodsIn(elements.getAllMembers(superClass))) {
            if (method.getSimpleName().contentEquals(methodName)) {
              return method;
            }
          }
          return null;
        })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  public static PlatformAdapter platform() {
    return readAdapter;
  }

  public static void setPlatform(PlatformAdapter platform) {
    readAdapter = platform;
  }

  public static String diAnnotation() {
    return diAnnotation;
  }
}
