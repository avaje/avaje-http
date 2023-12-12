package io.avaje.http.generator.core.javadoc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavadocParserTest {

  @Test
  public void Javadoc_parse() {

    Javadoc doc = Javadoc.parse(
      "This is a description\n" +
        "@return The return value");

    assertEquals("This is a description", doc.getSummary());
    assertEquals("The return value", doc.getReturnDescription());
  }

  @Test
  public void parse() {

    JavadocParser parser = new JavadocParser();

    Javadoc doc = parser.parse(
      "This is a description with <b>bold</b> and {@code some code}\n" +
        "@since 1.0\n" +
        "@param foo The foo param\n" +
        "@param bar The {@code bar} param\n" +
        "@return The {@value return} value\n");

    assertEquals("This is a description with bold and some code", doc.getSummary());
    assertEquals("The foo param", doc.getParams().get("foo"));
    assertEquals("The bar param", doc.getParams().get("bar"));
    assertEquals("The return value", doc.getReturnDescription());
  }

  @Test
  public void parse_multiline() {

    JavadocParser parser = new JavadocParser();

    Javadoc doc = parser.parse(
      "This is a description. This is more\n" +
        "  content1  \n" +
        "  content2  \n" +
        "@since 1.0\n" +
        "@param foo The foo param\n" +
        "@param bar The {@code bar} param\n" +
        "@return The {@value return} value\n");

    assertEquals("This is a description", doc.getSummary());
    assertEquals("This is more content1 content2", doc.getDescription());
    assertEquals("The foo param", doc.getParams().get("foo"));
    assertEquals("The bar param", doc.getParams().get("bar"));
    assertEquals("The return value", doc.getReturnDescription());
  }

  @Test
  public void parse_multiline_onlyOpenParagraphs() {

    Javadoc doc = Javadoc.parse(
    " Hello resource API.\n" +
      "<p>\n" +
      "Produces content.\n" +
      "<p>\n" +
      "What about this.");

    assertEquals("Hello resource API", doc.getSummary());
    assertEquals("Produces content. What about this.", doc.getDescription());
  }

  @Test
  public void parse_with_summaryAndDescription() {

    JavadocParser parser = new JavadocParser();

    Javadoc doc = parser.parse(
      "This is a description. With <b>bold</b> and {@code some code}\n" +
        "@since 1.0\n");

    assertEquals("This is a description", doc.getSummary());
    assertEquals("With bold and some code", doc.getDescription());
    assertFalse(doc.isDeprecated());
    assertTrue(doc.getParams().isEmpty());
    assertEquals("", doc.getReturnDescription());
  }

  @Test
  public void parse_with_deprecated() {

    JavadocParser parser = new JavadocParser();

    Javadoc doc = parser.parse(
      "Summary. Description\n" +
        "@deprecated\n");

    assertEquals("Summary", doc.getSummary());
    assertEquals("Description", doc.getDescription());
    assertTrue(doc.isDeprecated());
    assertTrue(doc.getParams().isEmpty());
    assertEquals("", doc.getReturnDescription());
  }


  @Test
  public void parse_with_deprecatedWithComment() {

    JavadocParser parser = new JavadocParser();

    Javadoc doc = parser.parse(
      "Summary. Description\n" +
        "@since 1.0\n" +
        "@deprecated Migration to something else\n");

    assertEquals("Summary", doc.getSummary());
    assertEquals("Description", doc.getDescription());
    assertTrue(doc.isDeprecated());
    assertTrue(doc.getParams().isEmpty());
    assertEquals("", doc.getReturnDescription());
  }
  @Test
  public void parse_returnNoEOL() {

    JavadocParser parser = new JavadocParser();

    Javadoc doc = parser.parse(
      "This is a description\n" +
        "@return The return value");

    assertEquals("This is a description", doc.getSummary());
    assertEquals("The return value", doc.getReturnDescription());
  }

  @Test
  public void lines() {

    JavadocParser parser = new JavadocParser();

    assertThat(parser.mergeLines("one\ntwo\nthree")).isEqualTo("one two three");
    assertThat(parser.mergeLines("\none\ntwo\nthree\n")).isEqualTo("one two three");
    assertThat(parser.mergeLines("\n one \n  two  \n three \n")).isEqualTo("one two three");

  }

  @Test
  public void parse_param_with_newline() {

    JavadocParser parser = new JavadocParser();

    Javadoc doc = parser.parse(
      "This is a description with <b>bold</b> and {@code some code}\n" +
        "@since 1.0\n" +
        "@param foo The\n foo param\n" +
        "@param bar The {@code \n} param\n" +
        "@return The {@value return}\n value\n");

    assertEquals("This is a description with bold and some code", doc.getSummary());
    assertEquals("The foo param", doc.getParams().get("foo"));
    assertEquals("The \\n param", doc.getParams().get("bar"));
    assertEquals("The return value", doc.getReturnDescription());
  }
}
