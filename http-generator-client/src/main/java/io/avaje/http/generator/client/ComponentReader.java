package io.avaje.http.generator.client;

import static io.avaje.http.generator.core.ProcessingContext.filer;
import static io.avaje.http.generator.core.ProcessingContext.logDebug;
import static io.avaje.http.generator.core.ProcessingContext.logWarn;
import static io.avaje.http.generator.core.ProcessingContext.typeElement;
import static java.util.stream.Collectors.toList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

import io.avaje.http.generator.core.APContext;
import io.avaje.http.generator.core.Constants;
import io.avaje.prism.GeneratePrism;

@GeneratePrism(io.avaje.http.api.spi.MetaData.class)
final class ComponentReader {

  private final ComponentMetaData componentMetaData;
  private final Map<String, ComponentMetaData> privateMetaData;

  ComponentReader(ComponentMetaData metaData, Map<String, ComponentMetaData> privateMetaData) {
    this.componentMetaData = metaData;
    this.privateMetaData = privateMetaData;
  }

  void read() {
    for (String fqn : loadMetaInf()) {
      final TypeElement moduleType = typeElement(fqn);
      if (moduleType != null) {
        var adapters =
          MetaDataPrism.getInstanceOn(moduleType).value().stream()
            .map(APContext::asTypeElement)
            .collect(toList());

        if (adapters.get(0).getModifiers().contains(Modifier.PUBLIC)) {
          componentMetaData.setFullName(fqn);
          adapters.stream()
            .map(TypeElement::getQualifiedName)
            .map(Object::toString)
            .forEach(componentMetaData::add);

        } else {
          var packageName = APContext.elements().getPackageOf(moduleType).getQualifiedName().toString();
          var meta = privateMetaData.computeIfAbsent(packageName, k -> new ComponentMetaData());
          adapters.stream()
            .map(TypeElement::getQualifiedName)
            .map(Object::toString)
            .forEach(meta::add);
        }
      }
    }
  }

  private Set<String> loadMetaInf() {
    var set = new HashSet<String>();
    try {
      addLines(mainMetaInfURI(), set);
      addLines(metaInfURI(), set);
    } catch (final IOException e) {
      logWarn("Error reading services file: " + e.getMessage());
    }
    return set;
  }

  private static void addLines(URI uri, HashSet<String> set) {
    try (var lines = Files.lines(Path.of(uri))) {
      lines.forEach(set::add);
    } catch (FileNotFoundException | NoSuchFileException e) {
      // logDebug("no services file yet");
    } catch (final FilerException e) {
      logDebug("FilerException reading services file");
    } catch (Exception e) {
      logWarn("Error reading services file: " + e.getMessage());
    }
  }

  private static URI mainMetaInfURI() throws IOException {
    return URI.create(
      metaInfURI()
        .toString()
        .replaceFirst("java/test", "java/main")
        .replaceFirst("test-classes", "classes"));
  }

  private static URI metaInfURI() throws IOException {
    return filer()
      .getResource(StandardLocation.CLASS_OUTPUT, "", Constants.META_INF_COMPONENT)
      .toUri();
  }
}
