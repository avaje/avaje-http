package io.avaje.http.generator.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClientProcessorTest {

  @AfterEach
  void deleteGeneratedFiles() throws IOException {
    try {
      Paths.get("io.avaje.http.client.HttpClient$GeneratedComponent")
          .toAbsolutePath()
          .toFile()
          .delete();
      Files.walk(Paths.get("io").toAbsolutePath())
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);

    } catch (final Exception e) {
    }
  }

  @Test
  void testGeneration() throws Exception {
    final Iterable<JavaFileObject> files = getSourceFiles("src/test/java/");

    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    final CompilationTask task =
        compiler.getTask(
            new PrintWriter(System.out), null, null, Arrays.asList("--release=11"), null, files);
    task.setProcessors(Arrays.asList(new ClientProcessor()));

    assertThat(task.call()).isTrue();
  }

  private Iterable<JavaFileObject> getSourceFiles(String source) throws Exception {
    final var compiler = ToolProvider.getSystemJavaCompiler();
    final var files = compiler.getStandardFileManager(null, null, null);

    files.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));

    final Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);
    return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
  }
}
