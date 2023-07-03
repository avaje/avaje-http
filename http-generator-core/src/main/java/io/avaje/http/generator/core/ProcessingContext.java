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

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

  private ProcessingContext() {}

  private static final class Ctx {
    private PlatformAdapter readAdapter;
    private final Messager messager;
    private final Filer filer;
    private final Elements elementUtils;
    private final Types typeUtils;
    private boolean openApiAvailable;
    private DocContext docContext;
    private final boolean useComponent;
    private final boolean useJavax;
    private final String diAnnotation;
    private final boolean instrumentAllMethods;
    private final boolean disableDirectWrites;
    private final boolean useJsonb;

    Ctx(ProcessingEnvironment env, PlatformAdapter adapter, boolean generateOpenAPI) {
      readAdapter = adapter;
      messager = env.getMessager();
      filer = env.getFiler();
      elementUtils = env.getElementUtils();
      typeUtils = env.getTypeUtils();

      if (generateOpenAPI) {
        openApiAvailable = elementUtils.getTypeElement(Constants.OPENAPIDEFINITION) != null;
        docContext = new DocContext(env, openApiAvailable);
      }

      final var options = env.getOptions();
      final var singletonOverride = options.get("useSingleton");
      this.instrumentAllMethods = Boolean.parseBoolean(options.get("instrumentRequests"));
      this.disableDirectWrites = Boolean.parseBoolean(options.get("disableDirectWrites"));
      if (singletonOverride != null) {
        useComponent = !Boolean.parseBoolean(singletonOverride);
      } else {
        useComponent = elementUtils.getTypeElement(Constants.COMPONENT) != null;
      }
      diAnnotation = (useComponent ? "@Component" : "@Singleton");
      useJsonb = elementUtils.getTypeElement("io.avaje.jsonb.Jsonb") != null;
      final var javax = elementUtils.getTypeElement(Constants.SINGLETON_JAVAX) != null;
      final var jakarta = elementUtils.getTypeElement(Constants.SINGLETON_JAKARTA) != null;
      final var override = options.get("useJavax");
      if (override != null || (javax && jakarta)) {
        useJavax = Boolean.parseBoolean(override);
      } else {
        useJavax = (javax);
      }
    }
  }

  public static void init(ProcessingEnvironment env, PlatformAdapter adapter, boolean generateOpenAPI) {
    final var oldCtx = CTX.get();
    final var newCTX = new Ctx(env, adapter, generateOpenAPI);
    if (oldCtx != null && newCTX.docContext == null) {
      newCTX.docContext = oldCtx.docContext;
    }
    CTX.set(newCTX);
  }

  public static void init(ProcessingEnvironment env, PlatformAdapter adapter) {
    init(env, adapter, true);
  }

  private static boolean isTypeAvailable(String canonicalName) {
    return null != typeElement(canonicalName);
  }

  public static TypeElement typeElement(String canonicalName) {
    return CTX.get().elementUtils.getTypeElement(canonicalName);
  }

  public static boolean isOpenApiAvailable() {
    return CTX.get().openApiAvailable;
  }

  public static boolean useJavax() {
    return CTX.get().useJavax;
  }

  public static boolean useComponent() {
    return CTX.get().useComponent;
  }

  public static void logError(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }

  public static void logError(Element e, String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  public static void logWarn(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args));
  }

  public static void logDebug(String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
  }
  /** Create a file writer for the given class name. */
  public static JavaFileObject createWriter(String cls, Element origin) throws IOException {
    return CTX.get().filer.createSourceFile(cls, origin);
  }

  /** Create a file writer for the META-INF services file. */
  public static FileObject createMetaInfWriter(String target) throws IOException {
    return CTX.get().filer.createResource(StandardLocation.CLASS_OUTPUT, "", target);
  }

  public static JavaFileObject createWriter(String cls) throws IOException {
    return CTX.get().filer.createSourceFile(cls);
  }

  public static String docComment(Element param) {
    return CTX.get().elementUtils.getDocComment(param);
  }

  public static DocContext doc() {
    return CTX.get().docContext;
  }

  public static TypeElement asElement(TypeMirror typeMirror) {
    return (TypeElement) CTX.get().typeUtils.asElement(typeMirror);
  }

  public static TypeMirror asMemberOf(DeclaredType declaredType, Element element) {
    return CTX.get().typeUtils.asMemberOf(declaredType, element);
  }

  public static List<ExecutableElement> superMethods(Element element, String methodName) {
    final Types types = CTX.get().typeUtils;
    return types.directSupertypes(element.asType()).stream()
      .filter(type -> !type.toString().contains("java.lang.Object"))
      .map(superType -> {
        final var superClass = (TypeElement) types.asElement(superType);
        for (final ExecutableElement method : ElementFilter.methodsIn(CTX.get().elementUtils.getAllMembers(superClass))) {
          if (method.getSimpleName().contentEquals(methodName)) {
            return method;
          }
        }
      return null;
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }

  public static PlatformAdapter platform() {
    return CTX.get().readAdapter;
  }

  public static void setPlatform(PlatformAdapter platform) {
    CTX.get().readAdapter = platform;
  }

  public static String diAnnotation() {
    return CTX.get().diAnnotation;
  }

  public static boolean instrumentAllWebMethods() {
    return CTX.get().instrumentAllMethods;
  }

  public static boolean useJsonb() {
    return CTX.get().useJsonb;
  }

  public static boolean disabledDirectWrites() {
    return CTX.get().disableDirectWrites;
  }

  public static Filer filer() {
    return CTX.get().filer;
  }
}
