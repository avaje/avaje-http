package io.avaje.http.generator;

import static org.assertj.core.api.Assertions.assertThat;

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

import io.avaje.http.generator.sigma.SigmaProcessor;
import io.avaje.jsonb.generator.JsonbProcessor;

class SigmaProcessorTest {

  @AfterEach
  void deleteGeneratedFiles() throws IOException {

    Paths.get("openapi.json").toAbsolutePath().toFile().delete();

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
    task.setProcessors(List.of(new SigmaProcessor()));

    assertThat(task.call()).isTrue();
    assert Files.readString(
            Paths.get("org/example/myapp/web/BarController$Route.java").toAbsolutePath())
        .contains("io.avaje.inject.Component");
  }

  @Test
  void runAnnotationProcessorJakarta() throws Exception {
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
    task.setProcessors(List.of(new SigmaProcessor(), new JsonbProcessor()));

    assertThat(task.call()).isTrue();

    assert Files.readString(
            Paths.get("org/example/myapp/web/BarController$Route.java").toAbsolutePath())
        .contains("jakarta.inject.Singleton");
  }

  private Iterable<JavaFileObject> getSourceFiles(String source) throws Exception {
    final var compiler = ToolProvider.getSystemJavaCompiler();
    final var files = compiler.getStandardFileManager(null, null, null);

    files.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));

    final Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);
    return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
  }
}
