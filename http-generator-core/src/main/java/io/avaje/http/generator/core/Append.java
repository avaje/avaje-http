package io.avaje.http.generator.core;

import java.io.IOException;
import java.io.Writer;

/**
 * Helper that wraps a writer with some useful methods to append content.
 */
public class Append {

  private static final boolean DEBUG = Boolean.getBoolean("append.debug");

  private final Writer writer;
  private final StringBuilder stringBuilder = new StringBuilder();

  public Append(Writer writer) {
    this.writer = writer;
  }

  public Append append(String content) {
    try {
      writer.append(content);
      if (DEBUG) {
        stringBuilder.append(content);
      }
      return this;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    try {
      writer.flush();
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Append eol() {
    try {
      writer.append("\n");
      if (DEBUG) {
        stringBuilder.append("\n");
      }
      return this;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Append content with formatted arguments.
   */
  public Append append(String format, Object... args) {
    return append(String.format(format, args));
  }

  @Override
  public String toString() {
    return stringBuilder.toString();
  }
}
