package io.avaje.http.generator.core;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

final class AnnotationCopier {
  private AnnotationCopier() {}

  private static final Pattern ANNOTATION_TYPE_PATTERN = Pattern.compile("@([\\w.]+)\\.");

  static String trimAnnotationString(String input) {
    return ANNOTATION_TYPE_PATTERN.matcher(input).replaceAll("@");
  }

  static void copyAnnotations(Append writer, Element element, boolean newLines) {
    copyAnnotations(writer, element, "", newLines);
  }

  static void copyAnnotations(Append writer, Element element, String indent, boolean newLines) {
    for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      final var type = annotationMirror.getAnnotationType().asElement().asType().toString();
      if (!type.contains("io.avaje.http.api.")
          || type.contains("Produces")
          || type.contains("Consumes")
          || type.contains("InstrumentServerContext")
          || type.contains("Default")
          || type.contains("OpenAPI")
          || type.contains("Valid")) {
        continue;
      }

      String annotationString = toAnnotationString(indent, annotationMirror, false);

      annotationString =
          annotationString
              .replace("io.avaje.http.api.", "")
              .replace("value=", "")
              .replace("(\"\")", "");

      writer.append(annotationString);

      if (newLines) {
        writer.eol();
      } else {
        writer.append(" ");
      }
    }
  }

  static String toSimpleAnnotationString(AnnotationMirror annotationMirror) {
    return trimAnnotationString(toAnnotationString("", annotationMirror, true)).substring(1);
  }

  static String toAnnotationString(
      String indent, AnnotationMirror annotationMirror, boolean simpleEnums) {
    final String annotationName = annotationMirror.getAnnotationType().toString();

    final StringBuilder sb =
        new StringBuilder(indent).append("@").append(annotationName).append("(");
    boolean first = true;

    for (final var entry : sortedValues(annotationMirror)) {
      if (!first) {
        sb.append(", ");
      }
      sb.append(entry.getKey().getSimpleName()).append("=");
      writeVal(sb, entry.getValue(), simpleEnums);
      first = false;
    }

    return sb.append(")").toString().replace("()", "");
  }

  private static List<Entry<? extends ExecutableElement, ? extends AnnotationValue>> sortedValues(
      AnnotationMirror annotationMirror) {
    return APContext.elements().getElementValuesWithDefaults(annotationMirror).entrySet().stream()
        .sorted(AnnotationCopier::compareBySimpleName)
        .collect(toList());
  }

  private static int compareBySimpleName(
      Entry<? extends ExecutableElement, ? extends AnnotationValue> entry1,
      Entry<? extends ExecutableElement, ? extends AnnotationValue> entry2) {
    return entry1
        .getKey()
        .getSimpleName()
        .toString()
        .compareTo(entry2.getKey().getSimpleName().toString());
  }

  @SuppressWarnings("unchecked")
  private static void writeVal(
      final StringBuilder sb, final AnnotationValue annotationValue, boolean simpleEnums) {
    final var value = annotationValue.getValue();
    if (value instanceof List) {
      // handle array values
      sb.append("{");
      boolean first = true;
      for (final AnnotationValue listValue : (List<AnnotationValue>) value) {
        if (!first) {
          sb.append(", ");
        }
        writeVal(sb, listValue, simpleEnums);
        first = false;
      }
      sb.append("}");

    } else if (value instanceof VariableElement) {
      // Handle enum values
      final var element = (VariableElement) value;
      final var type = element.asType();
      final var str = simpleEnums ? element : type.toString() + "." + element;
      sb.append(str);

    } else if (value instanceof AnnotationMirror) {
      // handle annotation values
      final var mirror = (AnnotationMirror) value;
      final String annotationName = mirror.getAnnotationType().toString();
      sb.append("@").append(annotationName).append("(");
      boolean first = true;

      for (final var entry : sortedValues(mirror)) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(entry.getKey().getSimpleName()).append("=");
        writeVal(sb, entry.getValue(), simpleEnums);
        first = false;
      }
      sb.append(")");

    } else {
      sb.append(annotationValue);
    }
  }
}
