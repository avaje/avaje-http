package io.avaje.http.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.avaje.http.generator.core.APContext;
import io.avaje.http.generator.javalin.JavalinProcessor;
import io.avaje.jsonb.generator.JsonbProcessor;

class JavalinProcessorTest {

  @AfterEach
  void deleteGeneratedFiles() throws IOException {
	APContext.clear();
    io.avaje.jsonb.generator.APContext.clear();
    Paths.get("openapi.json").toAbsolutePath().toFile().delete();
    Paths.get("io.avaje.jsonb.Jsonb$GeneratedComponent").toAbsolutePath().toFile().delete();

    Files.walk(Paths.get("org").toAbsolutePath())
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  @Test
  public void runAnnotationProcessor() throws Exception {
    final var source = Paths.get("src").toAbsolutePath().toString();

    final var files = getSourceFiles(source);

    final var compiler = ToolProvider.getSystemJavaCompiler();

    final var task =
        compiler.getTask(
            new PrintWriter(System.out),
            null,
            null,
            List.of("--release=11", "-AdisableDirectWrites=true"),
            null,
            files);
    task.setProcessors(List.of(new JavalinProcessor()));

    assertThat(task.call()).isTrue();
    assert Files.readString(
            Paths.get("org/example/myapp/web/BarController$Route.java").toAbsolutePath())
        .contains("io.avaje.inject.Component");
  }

  @Test
  public void runAnnotationProcessorJsonB() throws Exception {
    final var source = Paths.get("src").toAbsolutePath().toString();

    final var files = getSourceFiles(source);

    final var compiler = ToolProvider.getSystemJavaCompiler();

    final var task =
        compiler.getTask(
            new PrintWriter(System.out), null, null, List.of("--release=11"), null, files);
    task.setProcessors(List.of(new JavalinProcessor(), new JsonbProcessor()));

    assertThat(task.call()).isTrue();
    assert Files.readString(
            Paths.get("org/example/myapp/web/BarController$Route.java").toAbsolutePath())
        .contains("io.avaje.jsonb.Jsonb");
  }

  @Test
  public void runAnnotationProcessorJavax() throws Exception {
    final var source = Paths.get("src").toAbsolutePath().toString();

    final var files = getSourceFiles(source);

    final var compiler = ToolProvider.getSystemJavaCompiler();

    final var task =
        compiler.getTask(
            new PrintWriter(System.out),
            null,
            null,
            List.of(
                "--release=11",
                "-AuseJavax=true",
                "-AuseSingleton=true",
                "-AdisableDirectWrites=true"),
            null,
            files);
    task.setProcessors(List.of(new JavalinProcessor(), new JsonbProcessor()));
    // we don't have javax on the cp
    assertThat(task.call()).isFalse();

    assert Files.readString(
            Paths.get("org/example/myapp/web/BarController$Route.java").toAbsolutePath())
        .contains("javax.inject.Singleton");
  }

  @Test
  public void runAnnotationProcessorJakarta() throws Exception {
    final var source = Paths.get("src").toAbsolutePath().toString();

    final var files = getSourceFiles(source);

    final var compiler = ToolProvider.getSystemJavaCompiler();

    final var task =
        compiler.getTask(
            new PrintWriter(System.out),
            null,
            null,
            List.of(
                "--release=11",
                "-AuseJavax=false",
                "-AuseSingleton=true",
                "-AdisableDirectWrites=true"),
            null,
            files);
    task.setProcessors(List.of(new JavalinProcessor(), new JsonbProcessor()));

    assertThat(task.call()).isTrue();

    assert Files.readString(
            Paths.get("org/example/myapp/web/BarController$Route.java").toAbsolutePath())
        .contains("jakarta.inject.Singleton");
  }

  @Test
  public void testOpenAPIGeneration() throws Exception {
    final var source = Paths.get("src").toAbsolutePath().toString();
    // OpenAPIController
    final var files = getSourceFiles(source);

    Iterable<JavaFileObject> openAPIController = null;
    for (final var file : files) {
      if (file.isNameCompatible("OpenAPIController", Kind.SOURCE))
        openAPIController = List.of(file);
    }
    final var compiler = ToolProvider.getSystemJavaCompiler();

    final var task =
        compiler.getTask(
            new PrintWriter(System.out),
            null,
            null,
            List.of("--release=11", "-AdisableDirectWrites=true"),
            null,
            openAPIController);
    task.setProcessors(List.of(new JavalinProcessor(), new JsonbProcessor()));

    assertThat(task.call()).isTrue();

    final var mapper = new ObjectMapper();
    final var expectedOpenApiJson =
        mapper.readTree(new File("src/test/resources/expectedOpenApi.json"));
    final var generatedOpenApi = mapper.readTree(new File("openapi.json"));

    assertOpenApi31(generatedOpenApi.toString());
    assertThat(generatedOpenApi).isEqualTo(expectedOpenApiJson);
  }

  @Test
  public void testInheritableOpenAPIGeneration() throws Exception {
    final var source = Paths.get("src").toAbsolutePath().toString();
    // OpenAPIController
    final var files = getSourceFiles(source);

    final List<JavaFileObject> openAPIController = new ArrayList<>(2);
    for (final var file : files) {
      if (file.isNameCompatible("HealthController", Kind.SOURCE)
          || file.isNameCompatible("HealthControllerImpl", Kind.SOURCE))
        openAPIController.add(file);
    }
    final var compiler = ToolProvider.getSystemJavaCompiler();

    final var task =
        compiler.getTask(
            new PrintWriter(System.out),
            null,
            null,
            List.of("--release=11", "-AdisableDirectWrites=true"),
            null,
            openAPIController);
    task.setProcessors(List.of(new JavalinProcessor(), new JsonbProcessor()));

    assertThat(task.call()).isTrue();

    final var mapper = new ObjectMapper();
    final var expectedOpenApiJson =
        mapper.readTree(new File("src/test/resources/expectedInheritedOpenApi.json"));
    final var generatedOpenApi = mapper.readTree(new File("openapi.json"));

    assertOpenApi31(generatedOpenApi.toString());
    assertThat(generatedOpenApi).isEqualTo(expectedOpenApiJson);
  }

  private Iterable<JavaFileObject> getSourceFiles(String source) throws Exception {
    final var compiler = ToolProvider.getSystemJavaCompiler();
    final var files = compiler.getStandardFileManager(null, null, null);

    files.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));

    final Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);
    return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
  }

  private static void assertOpenApi31(String json) {
    assertThat(json).contains("\"openapi\":\"3.1.2\"");
    assertThat(json).contains("\"jsonSchemaDialect\":\"https://spec.openapis.org/oas/3.1/dialect/base\"");
    assertThat(json).doesNotContain("\"nullable\"");
    assertThat(json).doesNotContain("\"types\":");
  }
}
