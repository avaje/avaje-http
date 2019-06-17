package io.dinject.javalin.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dinject.controller.Controller;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

public class Processor extends AbstractProcessor {

  private ProcessingContext ctx;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_8;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {

    Set<String> annotations = new LinkedHashSet<>();
    annotations.add(Controller.class.getCanonicalName());
    annotations.add(OpenAPIDefinition.class.getCanonicalName());
    return annotations;
  }

  void logDebug(String msg, Object... args) {
    ctx.logDebug(msg, args);
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.ctx = new ProcessingContext(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {

    loadOpenAPI();

    logDebug("process controllers ...");

    if (ctx.isOpenApiAvailable()) {
      readOpenApiDefinition(round);
    }

    Set<? extends Element> controllers = round.getElementsAnnotatedWith(Controller.class);
    for (Element controller : controllers) {
      writeControllerAdapter(controller);
    }

    if (round.processingOver()) {
      logDebug("processingOver ...");
      writeOpenAPI();
    }
    return false;
  }

  private void readOpenApiDefinition(RoundEnvironment round) {

    Set<? extends Element> elements = round.getElementsAnnotatedWith(OpenAPIDefinition.class);
    for (Element element : elements) {

      Info info1 = ctx.getOpenAPI().getInfo();

      OpenAPIDefinition openApi = element.getAnnotation(OpenAPIDefinition.class);
      io.swagger.v3.oas.annotations.info.Info info = openApi.info();

      logDebug("reading OpenAPIDefinition " + openApi + " info:" + info);

      if (!info.title().isEmpty()) {
        info1.setTitle(info.title());
      }
      if (!info.description().isEmpty()) {
        info1.setDescription(info.description());
      }
    }
  }

  private void writeOpenAPI() {

    try (Writer metaWriter = createMetaWriter()) {

      OpenAPI openAPI = ctx.getOpenAPI();
      logDebug("openAPI writing paths: " + openAPI.getPaths().keySet());

      ObjectMapper mapper = createObjectMapper();
      mapper.writeValue(metaWriter, openAPI);

    } catch (IOException e) {
      ctx.logError(null, "Error writing openapi file" + e.getMessage());
      e.printStackTrace();
    }
  }

  private ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .enable(SerializationFeature.INDENT_OUTPUT);

    return mapper;
  }


  private Writer createMetaWriter() throws IOException {

    FileObject writer = ctx.createResource("meta", "swagger.json", null);
    return writer.openWriter();
  }


  private void writeControllerAdapter(Element controller) {
    if (controller instanceof TypeElement) {
      ControllerReader reader = new ControllerReader((TypeElement) controller, ctx);
      reader.read();
      try {
        ControllerRouteWriter writer = new ControllerRouteWriter(reader, ctx);
        writer.write();
      } catch (Exception e) {
        e.printStackTrace();
        ctx.logError(reader.getBeanType(), "Failed to write $route class");
      }
    }
  }

  private void loadOpenAPI() {
    logDebug("loading openAPI ...");
    OpenAPI openAPI = ctx.getOpenAPI();
    if (openAPI != null && !openAPI.getPaths().isEmpty()) {
      logDebug("openAPI already loaded ... " + openAPI.getPaths().keySet());
      return;
    }
    initOpenAPI();
  }

  private void initOpenAPI() {

    OpenAPI openAPI;
    openAPI = new OpenAPI();
    openAPI.setPaths(new Paths());
    openAPI.setInfo(new Info());

    ctx.logDebug("initialise empty OpenAPI");
    ctx.setOpenAPI(openAPI);
  }
}
