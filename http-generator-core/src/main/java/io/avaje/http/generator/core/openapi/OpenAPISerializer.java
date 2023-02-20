package io.avaje.http.generator.core.openapi;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

final class OpenAPISerializer {

  private OpenAPISerializer() {}

  /**
   * Converts the given object into a serialized string.
   *
   * @param obj the object to serialize
   * @return the serialized string
   * @throws IllegalAccessException if the fields of the object cannot be accessed
   */
  static String serialize(Object obj) throws IllegalAccessException {

    final Class<?> cls = obj.getClass();

    final var sb = new StringBuilder();
    var firstElement = true;
    // handle collections and maps differently to avoid module errors
    if (obj instanceof Collection) {
      sb.append("[");
      final var collection = (Collection) obj;
      for (final Object element : collection) {

        if (!firstElement) {
          sb.append(",");
        }

        write(sb, element);
        firstElement = false;
      }

      sb.append("]");

    } else {

      if (obj instanceof Map) {

        sb.append("{");
        final Map<?, ?> map = (Map<?, ?>) obj;
        for (final Map.Entry<?, ?> entry : map.entrySet()) {

          if (!firstElement) {
            sb.append(",");
          }
          sb.append("\"");
          sb.append(entry.getKey());
          sb.append("\" : ");

          write(sb, entry.getValue());
          firstElement = false;
        }
      } else {

        sb.append("{");

        final var fields = getAllFields(cls);

        var firstField = true;
        for (final Field field : fields) {

          // skip JsonIgnored fields
          if ("BIND_TYPE_AND_TYPES".equals(field.getName())
              || "COMPONENTS_SCHEMAS_REF".equals(field.getName())
              || "exampleSetFlag".equals(field.getName())
              || "types".equals(field.getName())
              || "specVersion".equals(field.getName())) {
            continue;
          }

          field.setAccessible(true);
          final var value = field.get(obj);
          if (value != null) {

            if (!firstField) {
              sb.append(",");
            }
            sb.append("\"");
            sb.append(field.getName());
            sb.append("\" : ");
            write(sb, value);
            firstField = false;
          }
        }
      }
      sb.append("}");
    }
    return sb.toString();
  }

  /**
   * Gets all the fields of the given class and its superclass. Will skip fields of java lang
   * classes
   *
   * @param clazz the class to get the fields for
   * @return an array of fields
   */
  static Field[] getAllFields(Class<?> clazz) {
    final var fields = clazz.getDeclaredFields();
    Class<?> superclass = clazz.getSuperclass();
    if (superclass.getCanonicalName().startsWith("java.")) {
      superclass = null;
    }
    if (superclass != null) {
      final var superFields = getAllFields(superclass);
      final var allFields = new Field[fields.length + superFields.length];
      System.arraycopy(fields, 0, allFields, 0, fields.length);
      System.arraycopy(superFields, 0, allFields, fields.length, superFields.length);
      return allFields;
    } else {
      return fields;
    }
  }

  static boolean isPrimitiveWrapperType(Object value) {

    return value instanceof Boolean
        || value instanceof Character
        || value instanceof Byte
        || value instanceof Short
        || value instanceof Integer
        || value instanceof Long
        || value instanceof Float
        || value instanceof Double;
  }

  /**
   * Extracts the primitive value from the given object if it is a wrapper for a primitive type.
   *
   * @param object the object to extract the value from
   * @return the primitive value if the object is a wrapper, the object itself otherwise
   */
  static Object extractPrimitiveValue(Object object) {
    if (object instanceof Boolean) {
      return (boolean) object;
    } else if (object instanceof Character) {
      return (char) object;
    } else if (object instanceof Byte) {
      return (byte) object;
    } else if (object instanceof Short) {
      return (short) object;
    } else if (object instanceof Integer) {
      return (int) object;
    } else if (object instanceof Long) {
      return (long) object;
    } else if (object instanceof Float) {
      return (float) object;
    } else if (object instanceof Double) {
      return (double) object;
    } else {
      return object;
    }
  }

  /**
   * Appends the given value to the string builder.
   *
   * @param sb the string builder to append to
   * @param value the value to append
   * @throws IllegalAccessException if the fields of the value object cannot be accessed
   */
  static void write(StringBuilder sb, Object value) throws IllegalAccessException {
    final var isprimitiveWrapper = isPrimitiveWrapperType(value);
    // Append primitive or string value as is
    if (value.getClass().isPrimitive() || value instanceof String || isprimitiveWrapper) {
      if (isprimitiveWrapper) {
        sb.append(extractPrimitiveValue(value));
      } else {
        sb.append("\"");
        sb.append(value.toString().replace("\"", "\\\""));
        sb.append("\"");
      }
    } else if (value.getClass().isEnum()) {
      sb.append("\"");
      sb.append(value.toString().replace("\"", "\\\""));
      sb.append("\"");
    } else {
      // Recursively handle other object types
      sb.append(serialize(value));
    }
  }
}
