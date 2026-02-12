package io.avaje.http.generator.core.openapi;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import io.avaje.http.generator.core.OpenAPIDefinitionPrism;
import io.avaje.http.generator.core.SecuritySchemePrism;
import io.avaje.http.generator.core.SecuritySchemesPrism;
import io.avaje.http.generator.core.TagPrism;
import io.avaje.http.generator.core.TagsPrism;
import io.avaje.http.generator.core.Util;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/** Context for building the OpenAPI documentation. */
public class DocContext {
  private final Set<String> TAGS_TOP_SET = new HashSet<>();
  private final Set<String> TAGS = new HashSet<>();
  private final boolean openApiAvailable;
  private final Elements elements;
  private final Filer filer;
  private final Messager messager;
  private final Map<String, PathItem> pathMap = new TreeMap<>();
  private final SchemaDocBuilder schemaBuilder;
  private final OpenAPI openAPI;

  public DocContext(ProcessingEnvironment env, boolean openApiAvailable) {
    this.openApiAvailable = openApiAvailable;
    this.elements = env.getElementUtils();
    this.filer = env.getFiler();
    this.messager = env.getMessager();
    this.schemaBuilder = new SchemaDocBuilder(env.getTypeUtils(), env.getElementUtils());
    this.openAPI = initOpenAPI();
  }

  public boolean isOpenApiAvailable() {
    return openApiAvailable;
  }

  private OpenAPI initOpenAPI() {
    OpenAPI openAPI = new OpenAPI();
    openAPI.setPaths(new Paths());

    Server server = new Server();
    server.setUrl("localhost:8080");
    server.setDescription("local testing");
    openAPI.addServersItem(server);

    Info info = new Info();
    info.setTitle("");
    info.setVersion("");
    openAPI.setInfo(info);

    return openAPI;
  }

  Schema toSchema(String rawType, Element element) {
    final var typeElement = elements.getTypeElement(rawType);
    final var varElement = elements.getTypeElement(Util.trimAnnotations(element.asType().toString()));
    if (typeElement == null) {
      // primitive types etc
      return schemaBuilder.toSchema(element.asType());
    }
    if (varElement != null) {
      return schemaBuilder.toSchema(element);
    }
    return schemaBuilder.toSchema(typeElement);
  }

  Content createContent(TypeMirror returnType, String mediaType) {
    return schemaBuilder.createContent(returnType, mediaType);
  }

  PathItem pathItem(String fullPath) {
    return pathMap.computeIfAbsent(fullPath, s -> new PathItem());
  }

  void addFormParam(Operation operation, String varName, Schema schema) {
    schemaBuilder.addFormParam(operation, varName, schema);
  }

  void addRequestBody(Operation operation, Schema schema, String mediaType, String description) {
    schemaBuilder.addRequestBody(operation, schema, mediaType, description);
  }

  /**
   * Return the OpenAPI adding the paths and schemas.
   */
  private OpenAPI getApiForWriting() {
    Paths paths = openAPI.getPaths();
    if (paths == null) {
      paths = new Paths();
      openAPI.setPaths(paths);
    }
    // add paths by natural order
    for (Map.Entry<String, PathItem> entry : pathMap.entrySet()) {
      paths.addPathItem(entry.getKey(), entry.getValue());
    }

    components().setSchemas(schemaBuilder.getSchemas());
    return openAPI;
  }

  /**
   * Return the components creating if needed.
   */
  private Components components() {
    Components components = openAPI.getComponents();
    if (components == null) {
      components = new Components();
      openAPI.setComponents(components);
    }
    return components;
  }

  private Tag createTagItem(TagPrism tag) {
    final var tagsItem = new Tag();
    tagsItem.setName(tag.name());
    tagsItem.setDescription(tag.description());
    // tagsItem.setExtensions(tag.extensions());  # Not sure about the extensions
    // tagsItem.setExternalDocs(tag.externalDocs()); # Not sure about the external docs
    return tagsItem;
  }

  public void addTagsDefinition(Element element) {
    final var tags = TagsPrism.getInstanceOn(element);
    if (tags == null) {
      return;
    }
    for (var tag : tags.value()) {
      if (TAGS.add(tag.name())) {
        openAPI.addTagsItem(createTagItem(tag));
      }
    }
  }

  public void addTagDefinition(Element element) {
    for (var tag : TagPrism.getAllInstancesOn(element)) {
      if (TAGS_TOP_SET.add(tag.name())) {
        openAPI.addTagsItem(createTagItem(tag));
      }
    }
  }

  public void addSecurityScheme(Element element) {
    this.addSecuritySchemes(SecuritySchemePrism.getAllInstancesOn(element));
  }

  public void addSecuritySchemes(Element element) {
    var schemes = SecuritySchemesPrism.getInstanceOn(element);
    if (schemes == null) {
      return;
    }
    this.addSecuritySchemes(schemes.value());
  }

  void addSecuritySchemes(List<SecuritySchemePrism> schemes) {
    for (SecuritySchemePrism p : schemes) {
      var ss = new SecurityScheme()
        .type(SecurityScheme.Type.valueOf(p.type()))
        .in(SecurityScheme.In.valueOf(p.in()))
        .name(p.paramName());

      if (!p.description().isEmpty()) {
        ss.description(p.description());
      }
      if (!p.bearerFormat().isEmpty()) {
        ss.bearerFormat(p.bearerFormat());
      }
      if (!p.scheme().isEmpty()) {
        ss.scheme(p.scheme());
      }
      components().addSecuritySchemes(p.name(), ss);
    }
  }

  public void readApiDefinition(Element element) {
    final var openApi = OpenAPIDefinitionPrism.getInstanceOn(element);
    final var info = openApi.info();
    if (!info.title().isEmpty()) {
      openAPI.getInfo().setTitle(info.title());
    }
    if (!info.description().isEmpty()) {
      openAPI.getInfo().setDescription(info.description());
    }
    if (!info.version().isEmpty()) {
      openAPI.getInfo().setVersion(info.version());
    }
  }

  public void writeApi() {
    try (var metaWriter = createMetaWriter()) {
      final var json = OpenAPISerializer.serialize(getApiForWriting());
      JsonFormatter.prettyPrintJson(metaWriter, json);

    } catch (final Exception e) {
      logError(null, "Error writing openapi file" + e.getMessage());
    }
  }

  private Writer createMetaWriter() throws IOException {
    FileObject writer = filer.createResource(StandardLocation.CLASS_OUTPUT, "meta", "openapi.json");
    return writer.openWriter();
  }

  private void logError(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }
}
