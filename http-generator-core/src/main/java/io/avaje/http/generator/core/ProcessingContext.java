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

  private static ThreadLocal<PlatformAdapter> READ_ADAPTER = new ThreadLocal<>();
  private static ThreadLocal<Messager> MESSAGER = new ThreadLocal<>();
  private static ThreadLocal<Filer> FILER = new ThreadLocal<>();
  private static ThreadLocal<Elements> ELEMENTS = new ThreadLocal<>();
  private static ThreadLocal<Types> TYPES = new ThreadLocal<>();
  private static ThreadLocal<Boolean> OPENAPI_AVAILABLE = new ThreadLocal<>();
  private static ThreadLocal<DocContext> DOC_CONTEXT = new ThreadLocal<>();
  private static ThreadLocal<Boolean> USE_COMPONENT = new ThreadLocal<>();
  private static ThreadLocal<Boolean> USE_JAVAX = new ThreadLocal<>();
  private static ThreadLocal<String> DI_ANNOTATION = new ThreadLocal<>();

  public static void init(ProcessingEnvironment env, PlatformAdapter adapter) {
    init(env, adapter, true);
  }

  public static void init(
      ProcessingEnvironment env, PlatformAdapter adapter, boolean generateOpenAPI) {
    READ_ADAPTER.set(adapter);
    MESSAGER.set(env.getMessager());
    FILER.set(env.getFiler());
    ELEMENTS.set(env.getElementUtils());
    TYPES.set(env.getTypeUtils());

    if (generateOpenAPI) {
      OPENAPI_AVAILABLE.set(isTypeAvailable(Constants.OPENAPIDEFINITION));
      DOC_CONTEXT.set(new DocContext(env, OPENAPI_AVAILABLE.get()));
    }

    final var options = env.getOptions();
    final var singletonOverride = options.get("useSingleton");
    if (singletonOverride != null) {
      USE_COMPONENT.set(!Boolean.parseBoolean(singletonOverride));
    } else {
      USE_COMPONENT.set(isTypeAvailable(Constants.COMPONENT));
    }
    DI_ANNOTATION.set(USE_COMPONENT.get() ? "@Component" : "@Singleton");

    final var javax = isTypeAvailable(Constants.SINGLETON_JAVAX);
    final var jakarta = isTypeAvailable(Constants.SINGLETON_JAKARTA);
    final var override = env.getOptions().get("useJavax");
    if (override != null || (javax && jakarta)) {
      USE_JAVAX.set(Boolean.parseBoolean(override));
    } else {
      USE_JAVAX.set(javax);
    }
  }

  private static boolean isTypeAvailable(String canonicalName) {
    return null != typeElement(canonicalName);
  }

  public static TypeElement typeElement(String canonicalName) {
    return ELEMENTS.get().getTypeElement(canonicalName);
  }

  public static boolean isOpenApiAvailable() {
    return OPENAPI_AVAILABLE.get();
  }

  public static boolean useJavax() {
    return USE_JAVAX.get();
  }

  public static boolean useComponent() {
    return USE_COMPONENT.get();
  }

  public static void logError(Element e, String msg, Object... args) {
    MESSAGER.get().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  /** Create a file writer for the given class name. */
  public static JavaFileObject createWriter(String cls, Element origin) throws IOException {
    return FILER.get().createSourceFile(cls, origin);
  }

  /** Create a file writer for the META-INF services file. */
  public static FileObject createMetaInfWriter(String target) throws IOException {
    return FILER.get().createResource(StandardLocation.CLASS_OUTPUT, "", target);
  }

  public static String docComment(Element param) {
    return ELEMENTS.get().getDocComment(param);
  }

  public static DocContext doc() {
    return DOC_CONTEXT.get();
  }

  public static Element asElement(TypeMirror typeMirror) {
    return TYPES.get().asElement(typeMirror);
  }

  public static TypeMirror asMemberOf(DeclaredType declaredType, Element element) {
    return TYPES.get().asMemberOf(declaredType, element);
  }

  public static List<ExecutableElement> superMethods(Element element, String methodName) {
    final Types types = TYPES.get();
    return types.directSupertypes(element.asType()).stream()
        .filter(type -> !type.toString().contains("java.lang.Object"))
        .map(
            superType -> {
              final var superClass = (TypeElement) types.asElement(superType);
              for (final ExecutableElement method :
                  ElementFilter.methodsIn(ELEMENTS.get().getAllMembers(superClass))) {
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
    return READ_ADAPTER.get();
  }

  public static void setPlatform(PlatformAdapter platform) {
    READ_ADAPTER.set(platform);
  }

  public static String diAnnotation() {
    return DI_ANNOTATION.get();
  }
}
