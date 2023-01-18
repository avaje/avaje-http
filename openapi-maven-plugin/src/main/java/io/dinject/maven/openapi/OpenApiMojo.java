package io.dinject.maven.openapi;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Mojo(name = "openapi", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class OpenApiMojo extends AbstractMojo {

  /**
   * Relative path to find the generated openapi.json file.
   */
  @Parameter(name = "source")
  String source;

  /**
   * Relative path to put the openapi.json.
   */
  @Parameter(name = "destination")
  String destination;

  /**
   * The directory holding the class files we want to transform.
   */
  @Parameter(property = "project.build.outputDirectory")
  String buildOut;

  @Parameter(property = "project.build.resources[0].directory")
  String buildResourceDir;

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  @Override
  public void execute() {

    if (source == null) {
      // the location that the APT javalin-generator writes to
      source = "meta/openapi.json";
    }
    if (destination == null) {
      // location in src/main/resources to move openapi doc to
      destination = "public/openapi.json";
    }

    File sourceFile = new File(new File(buildOut), source);
    if (!sourceFile.exists()) {
      getLog().warn("openapi file not found at  " + sourceFile);
      return;
    }

    File srcMainRes = new File(buildResourceDir);
    if (destination != null) {
      File destFile = new File(srcMainRes, destination);
      File destDir = destFile.getParentFile();
      if (!destDir.exists() && !destDir.mkdirs()) {
        getLog().error("Failed to make directory " + destDir);
      } else {
        try {
          moveToMainResources(sourceFile, destFile);
        } catch (IOException e) {
          getLog().error("Failed to copy openapi file", e);
        }
      }
    }
  }

  /**
   * Copy the file to src/main/resources ...
   */
  private void moveToMainResources(File sourceFile, File destFile) throws IOException {
    // copy into src/main/resources ...
    Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    getLog().info("copied openapi file to " + destFile);
    if (!sourceFile.delete()) {
      getLog().warn("Failed to delete the temporary openapi file " + sourceFile);
    } else {
      deleteParentIfEmpty(sourceFile);
    }
  }

  /**
   * Delete the parent meta directory if it is empty.
   */
  private void deleteParentIfEmpty(File sourceFile) {
    final File meta = sourceFile.getParentFile();
    final String[] list = meta.list();
    if (list != null && list.length == 0) {
      if (meta.delete()) {
        getLog().debug("deleted empty meta directory");
      }
    }
  }
}
