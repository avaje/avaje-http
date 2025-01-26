package io.avaje.http.generator.client;

import static io.avaje.http.generator.core.ProcessingContext.createMetaInfWriter;
import static io.avaje.http.generator.core.ProcessingContext.createWriter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.Constants;
import io.avaje.http.generator.core.Util;

final class SimpleComponentWriter {

  private static final String AT_GENERATED = "@Generated(\"avaje-client-generator\")";

  private final ComponentMetaData metaData;
  private final Set<String> importTypes = new TreeSet<>();
  private Append writer;
  private JavaFileObject fileObject;
  private String fullName;

  SimpleComponentWriter(ComponentMetaData metaData) {
    this.metaData = metaData;
  }

  void init() throws IOException {
    if (fullName == null) {
      this.fullName = metaData.fullName();
    }
    if (fileObject == null) {
      fileObject = createWriter(metaData.fullName());
    }
  }

  void write() throws IOException {
    writer = new Append(fileObject.openWriter());
    writePackage();
    writeImports();
    writeClassStart();
    writeRegister();
    writeClassEnd();
    writer.close();
  }

  private void writeRegister() {
    writer.append("  @Override").eol();
    writer.append("  public void register(Map<Class<?>, HttpApiProvider<?>> providerMap) {").eol();

    for (final String clientFullName : metaData.all()) {
      final String clientShortName = Util.shortName(clientFullName);
      final String clientInterface = ClientSuffix.removeSuffix(clientShortName);
      writer.append("    providerMap.put(%s.class, %s::new);", clientInterface, clientShortName).eol();
    }
    writer.append("  }").eol().eol();
  }

  private void writeClassEnd() {
    writer.append("}").eol();
  }

  private void writeClassStart() {
    final String shortName = Util.shortName(fullName);
    writer.append(AT_GENERATED).eol();
    writer.append("@MetaData({");
    final List<String> all = metaData.all();
    writeMetaDataEntry(all);
    writer.append("})").eol();
    writer.append("public final class %s implements HttpClient.GeneratedComponent {", shortName).eol().eol();
  }

  private void writeMetaDataEntry(List<String> entries) {
    for (int i = 0, size = entries.size(); i < size; i++) {
      if (i > 0) {
        writer.append(", ");
      }
      writer.append("%s.class", Util.shortName(entries.get(i)));
    }
  }

  private void writeImports() {
    importTypes.add("io.avaje.http.client.HttpClient");
    importTypes.add("io.avaje.http.client.HttpApiProvider");
    importTypes.add("java.util.Map");
    importTypes.add("io.avaje.http.api.Generated");
    importTypes.add("io.avaje.http.api.spi.MetaData");
    importTypes.addAll(metaData.allImports());

    for (final String importType : importTypes) {
      writer.append("import %s;", importType).eol();
    }
    writer.eol();
  }

  private void writePackage() {
    final String packageName = TopPackage.packageOf(fullName);
    if (!packageName.isEmpty()) {
      writer.append("package %s;", packageName).eol().eol();
    }
  }
}
