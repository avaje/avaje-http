package io.avaje.http.generator;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.avaje.http.generator.core.APContext;
import io.avaje.http.generator.vertx.VertxProcessor;

class VertxProcessorTest {

  @AfterEach
  void deleteGeneratedFiles() throws IOException {
    APContext.clear();

    if (Files.exists(Paths.get("openapi.json"))) {
      Files.delete(Paths.get("openapi.json"));
    }

    if (Files.exists(Paths.get("controllers.txt"))) {
      Files.delete(Paths.get("controllers.txt"));
    }

    try (var generatedTxt = Files.list(Paths.get("."))) {
      for (var path : generatedTxt
        .filter(p -> p.getFileName().toString().endsWith("TestAPI.txt"))
        .toList()) {
        Files.delete(path);
      }
    }

    if (Files.exists(Paths.get("org"))) {
      Files.walk(Paths.get("org").toAbsolutePath())
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }

  @Test
  void runAnnotationProcessor() throws Exception {
    final var source = Paths.get("src").toAbsolutePath().toString();
    final var files = getSourceFiles(source);
    final var compiler = ToolProvider.getSystemJavaCompiler();

    final var task =
        compiler.getTask(
                new PrintWriter(System.out),
                null,
                null,
                List.of("--release=" + Integer.getInteger("java.specification.version")),
                null,
                files);
    task.setProcessors(List.of(new VertxProcessor()));

    assertThat(task.call()).isTrue();

    final var generatedSource =
        Files.readString(Paths.get("org/example/myapp/web/VertxRolesFixtureController$Route.java").toAbsolutePath());
    final var helloGeneratedSource =
        Files.readString(Paths.get("org/example/myapp/web/HelloController$Route.java").toAbsolutePath());

    assertThat(generatedSource)
        .contains("implements VertxRouteSet")
        .contains("import io.vertx.ext.web.handler.BodyHandler;")
        .contains("public void register(Router router)")
        .contains("route.handler(AuthorizationHandler.create(RoleBasedAuthorization.create(\"org.example.myapp.web.TestRole.ADMIN\")));")
        .contains("route.handler(AuthorizationHandler.create(OrAuthorization.create()");
    assertThat(generatedSource)
        .contains("var route = routes.post(\"/roles-test/payload\");")
        .contains("route.handler(BodyHandler.create());")
        .contains("ctx.body().asPojo(org.example.myapp.web.VertxRolesFixtureController.Payload.class)")
        .contains("ctx.response().end(Json.encode(result));")
        .contains(".addAuthorization(RoleBasedAuthorization.create(\"org.example.myapp.web.TestRole.ADMIN\"))")
        .contains(".addAuthorization(RoleBasedAuthorization.create(\"org.example.myapp.web.TestRole.AUDITOR\"))");

    final var payloadRouteIndex = generatedSource.indexOf("var route = routes.post(\"/roles-test/payload\");");
    final var payloadBodyHandlerIndex =
        generatedSource.indexOf("route.handler(BodyHandler.create());", payloadRouteIndex);
    final var payloadAuthHandlerIndex =
        generatedSource.indexOf(
            "route.handler(AuthorizationHandler.create(RoleBasedAuthorization.create(\"org.example.myapp.web.TestRole.ADMIN\")));",
            payloadRouteIndex);
    assertThat(payloadRouteIndex).isGreaterThan(-1);
    assertThat(payloadBodyHandlerIndex).isGreaterThan(payloadRouteIndex);
    assertThat(payloadAuthHandlerIndex).isGreaterThan(payloadRouteIndex);
    assertThat(payloadBodyHandlerIndex).isLessThan(payloadAuthHandlerIndex);

    assertThat(generatedSource)
        .contains("io.vertx.core.Handler<RoutingContext> filterHandler = ctx -> {")
        .contains("controller.filter(ctx);")
        .contains("if (!ctx.response().ended() && !ctx.failed()) {")
        .contains("routes.route(\"/roles-test\").handler(filterHandler);")
        .contains("routes.route(\"/roles-test/*\").handler(filterHandler);");
    assertThat(generatedSource)
        .contains("if (failure == null || !(failure instanceof IllegalArgumentException ex)) {")
        .contains("var result = controller.onIllegalArg(ex);");
    assertThat(generatedSource)
        .contains("if (failure == null || !(failure instanceof IllegalStateException ex)) {")
        .contains("ctx.response().setStatusCode(503);")
        .contains("controller.onIllegalState(ctx);")
        .contains("routes.route(\"/roles-test\").failureHandler(errorHandler);")
        .contains("routes.route(\"/roles-test/*\").failureHandler(errorHandler);");

    assertThat(helloGeneratedSource)
        .contains("var route = routes.get(\"/hello/with-params/:id\");")
        .contains("ctx.pathParam(\"id\")")
        .contains("ctx.request().getParam(\"q\")")
        .contains("ctx.request().getHeader(\"X-Trace\")");

    final var mapper = new ObjectMapper();
    final var expectedOpenApi = mapper.readTree(new File("src/test/resources/expectedOpenApi.json"));
    final var generatedOpenApi = mapper.readTree(new File("openapi.json"));
    assertThat(generatedOpenApi).isEqualTo(expectedOpenApi);
  }

  private Iterable<JavaFileObject> getSourceFiles(String source) throws Exception {
    final var compiler = ToolProvider.getSystemJavaCompiler();
    final var files = compiler.getStandardFileManager(null, null, null);

    files.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));

    final Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);
    return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
  }
}
