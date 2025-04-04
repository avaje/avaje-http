package io.avaje.http.generator.core;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
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

public final class ProcessingContext {

  private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

  private static final boolean ZERO_JSTACHIO = APContext.typeElement("io.jstach.jstachio.JStachio") == null;
  private static final Map<String, String> jstacheRenderers = new HashMap<>();

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
    private final boolean disableJsonB;
    private final boolean javalin6;
    private final Set<String> clientFQN = new HashSet<>();

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
      this.disableJsonB = Boolean.parseBoolean(options.get("disableJsonB"));
      if (singletonOverride != null) {
        useComponent = !Boolean.parseBoolean(singletonOverride);
      } else {
        useComponent = elementUtils.getTypeElement(Constants.COMPONENT) != null;
      }
      diAnnotation = (useComponent ? "@Component" : "@Singleton");

      final var javax = elementUtils.getTypeElement(Constants.SINGLETON_JAVAX) != null;
      final var jakarta = elementUtils.getTypeElement(Constants.SINGLETON_JAKARTA) != null;
      final var override = options.get("useJavax");
      if (override != null || (javax && jakarta)) {
        useJavax = Boolean.parseBoolean(override);
      } else {
        useJavax = (javax);
      }
      this.javalin6 = elementUtils.getTypeElement("io.javalin.config.RouterConfig") != null;
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

  public static void logWarn(Element e, String msg, Object... args) {
    CTX.get().messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);
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
    return filer().createResource(StandardLocation.CLASS_OUTPUT, "", target);
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
        for (final var method : ElementFilter.methodsIn(CTX.get().elementUtils.getAllMembers(superClass))) {
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
    return isJsonbInClasspath() && !CTX.get().disableJsonB;
  }

  private static boolean isJsonbInClasspath() {
    try {
      return CTX.get().elementUtils.getTypeElement("io.avaje.jsonb.Jsonb") != null
        || Class.forName("io.avaje.jsonb.Jsonb") != null;
    } catch (final ClassNotFoundException e) {
      return false;
    }
  }

  public static boolean disabledDirectWrites() {
    return CTX.get().disableDirectWrites;
  }

  public static boolean javalin6() {
    return CTX.get().javalin6;
  }

  public static Filer filer() {
    return CTX.get().filer;
  }

  public static boolean isAssignable2Interface(String type, String superType) {
    return type.equals(superType)
        || Optional.ofNullable(typeElement(type)).stream()
            .flatMap(ProcessingContext::superTypes)
            .anyMatch(superType::equals);
  }

  static Stream<String> superTypes(Element element) {
    final Types types = CTX.get().typeUtils;
    return types.directSupertypes(element.asType()).stream()
      .filter(type -> !type.toString().contains("java.lang.Object"))
      .map(superType -> (TypeElement) types.asElement(superType))
      .flatMap(e -> Stream.concat(superTypes(e), Stream.of(e)))
      .map(Object::toString);
  }

  public static void validateModule() {
    var module = APContext.getProjectModuleElement();
    if (module != null && !module.isUnnamed()) {
      try (var bufferedReader = APContext.getModuleInfoReader()) {
        var reader = new ModuleInfoReader(module, bufferedReader);
        reader.requires().forEach(r -> {
          if (!r.isStatic() && r.getDependency().getQualifiedName().contentEquals("io.avaje.http.api.javalin")) {
            logWarn(module, "io.avaje.http.api.javalin only contains SOURCE retention annotations. It should added as `requires static`");
          }
        });

        reader.validateServices("io.avaje.http.client.HttpClient.GeneratedComponent", CTX.get().clientFQN);

      } catch (Exception e) {
        // can't read module
      }
    }
  }

  static ModuleElement getModuleElement(Element e) {
    if (e == null || e instanceof ModuleElement) {
      return (ModuleElement) e;
    }
    return getModuleElement(e.getEnclosingElement());
  }

  static Elements elements() {
    return CTX.get().elementUtils;
  }

  private static boolean buildPluginAvailable() {
    return resourceExists("target/avaje-plugin-exists.txt")
      || resourceExists("build/avaje-plugin-exists.txt");
  }

  private static boolean resourceExists(String relativeName) {
    try {
      final String resource =
        filer()
          .getResource(StandardLocation.CLASS_OUTPUT, "", relativeName)
          .toUri()
          .toString()
          .replaceFirst("/target/classes", "")
          .replaceFirst("/build/classes/java/main", "");
      return Paths.get(new URI(resource)).toFile().exists();
    } catch (final Exception e) {
      return false;
    }
  }

  public static void addClientComponent(String clientFQN) {
    CTX.get().clientFQN.add(clientFQN);
  }

  public static String jstacheRenderer(TypeMirror typeMirror) {
    final var typeElement = APContext.asTypeElement(typeMirror);
    final var typeName = typeElement.getSimpleName().toString();
    return jstacheRenderers.computeIfAbsent(typeName, k -> determineJstacheRenderer(typeElement));
  }

  private static String determineJstacheRenderer(TypeElement typeElement) {
    return ZERO_JSTACHIO
      ? jstacheTypeRenderer(typeElement)
      : checkJstacheConfig(typeElement, typeElement);
  }

  private static String checkJstacheConfig(Element element, TypeElement typeElement) {
    if (element == null) {
      return "JStachio.render";
    }
    var config = JStacheConfigPrism.getInstanceOn(element);
    if (config != null && "STACHE".equals(config.type())) {
      return jstacheTypeRenderer(typeElement);
    } else if (config != null && "JSTACHIO".equals(config.type())) {
      return "JStachio.render";
    }

    return checkJstacheConfig(element.getEnclosingElement(), typeElement);
  }

  private static String jstacheTypeRenderer(TypeElement typeElement) {
    return typeElement.getSimpleName() + "Renderer.of().execute";
  }

  public static boolean isJstacheTemplate(TypeMirror mirror) {
    final var typeElement = APContext.asTypeElement(mirror);
    return typeElement != null && JStachePrism.isPresent(typeElement);
  }
}
