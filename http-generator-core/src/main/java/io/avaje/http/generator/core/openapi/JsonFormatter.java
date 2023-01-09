package io.avaje.http.generator.core.openapi;

import java.io.IOException;
import java.io.Writer;

final class JsonFormatter {

  private JsonFormatter() {}

  public static String prettyPrintJson(Writer writer, String json) throws IOException {
    final var jsonChars = json.toCharArray();

    var indentLevel = 0;
    var inString = false;

    for (var i = 0; i < jsonChars.length; i++) {
      final var current = jsonChars[i];

      if (current == '"' && jsonChars[i - 1] != '\\') {
        inString = !inString;
        writer.append(current);
      } else if (inString) {
        writer.append(current);
      } else {
        switch (current) {
          case '{':
          case '[':
            writer.append(current).append("\n");
            indentLevel++;
            addIndents(writer, indentLevel);
            break;
          case '}':
          case ']':
            writer.append("\n");
            indentLevel--;
            addIndents(writer, indentLevel);
            writer.append(current);
            break;
          case ',':
            writer.append(current).append("\n");
            addIndents(writer, indentLevel);
            break;
          default:
            writer.append(current);
            break;
        }
      }
    }

    return writer.toString();
  }

  private static void addIndents(Writer writer, int level) throws IOException {
    for (var i = 0; i < level; i++) {
      writer.append("\t");
    }
  }
}
