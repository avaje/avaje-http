package io.avaje.http.generator.core.openapi;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import io.avaje.http.generator.core.OpenAPIDefinitionPrism;
import io.avaje.http.generator.core.TagPrism;
import io.avaje.http.generator.core.TagsPrism;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.tags.Tag;

/** Context for building the OpenAPI documentation. */
public class DocContext {

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

    Info info = new Info();
    info.setTitle("");
    info.setVersion("");
    openAPI.setInfo(info);

    return openAPI;
  }

  Schema toSchema(String rawType, Element element) {
    TypeElement typeElement = elements.getTypeElement(rawType);
    if (typeElement == null) {
      // primitive types etc
      return schemaBuilder.toSchema(element.asType());
    } else {
      return schemaBuilder.toSchema(typeElement.asType());
    }
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

  void addRequestBody(Operation operation, Schema schema, boolean asForm, String description) {
    schemaBuilder.addRequestBody(operation, schema, asForm, description);
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
    if (tags == null) return;

    for(var tag : tags.value()){
      openAPI.addTagsItem(createTagItem(tag));
    }
  }

  public void addTagDefinition(Element element) {
    final var tag = TagPrism.getInstanceOn(element);
    if (tag == null) return;

    openAPI.addTagsItem(createTagItem(tag));
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

    final var openAPI = getApiForWriting();
    try (var metaWriter = createMetaWriter()) {

      final var json = OpenAPISerializer.serialize(openAPI);
      JsonFormatter.prettyPrintJson(metaWriter, json);

    } catch (final Exception e) {
      logError(null, "Error writing openapi file" + e.getMessage());
      e.printStackTrace();
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
