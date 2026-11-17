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

  @Test
  public void parse_param_description_on_next_line() {

    var parser = new JavadocParser();
    var doc =
        parser.parse(
            " Summary. A more detailed description.\n"
                + "\n"
                + " @param param1\n"
                + "         the first parameter\n"
                + " @param param2\n"
                + "         the second parameter\n"
                + "\n"
                + " @return\n"
                + "         the result\n");

    assertEquals("Summary", doc.getSummary());
    assertEquals("A more detailed description.", doc.getDescription());
    assertThat(doc.getParams()).containsOnlyKeys("param1", "param2");
    assertEquals("the first parameter", doc.getParams().get("param1"));
    assertEquals("the second parameter", doc.getParams().get("param2"));
    assertEquals("the result", doc.getReturnDescription());
  }

  @Test
  public void parse_throws_description_on_next_line() {

    var parser = new JavadocParser();
    var doc =
        parser.parse(
            " Summary. A detailed description.\n"
                + "\n"
                + " @param param1\n"
                + "         the first parameter\n"
                + " @param param2\n"
                + "         the second parameter\n"
                + "\n"
                + " @return the result\n"
                + "\n"
                + " @throws IOException\n"
                + "         if something bad happens\n"
                + " @throws FormatException\n"
                + "         if something other bad happens\n");

    assertEquals("Summary", doc.getSummary());
    assertEquals("A detailed description.", doc.getDescription());
    assertThat(doc.getParams()).containsOnlyKeys("param1", "param2");
    assertEquals("the first parameter", doc.getParams().get("param1"));
    assertEquals("the second parameter", doc.getParams().get("param2"));
    assertEquals("the result", doc.getReturnDescription());
  }

  @Test
  public void parse_multiline_return_and_trailing_tags() {

    var doc =
        Javadoc.parse(
            "Summary. Description.\n"
                + "@return the\n"
                + "   result {@link Foo#bar() bar}\n"
                + "@see Other\n"
                + "@apiNote some note\n"
                + "   continued\n");

    assertEquals("Summary", doc.getSummary());
    assertEquals("Description.", doc.getDescription());
    assertEquals("the result bar", doc.getReturnDescription());
  }

  @Test
  public void parse_code_with_annotation_at_line_start() {

    var doc = Javadoc.parse("Summary. Use like {@code\n@Get\n}\n@return the result\n");

    assertEquals("Summary", doc.getSummary());
    assertEquals("Use like @Get\\n", doc.getDescription());
    assertEquals("the result", doc.getReturnDescription());
  }

  /** Parse the text as a return description to observe the inline tag output. */
  private static String inlined(String text) {
    return Javadoc.parse("Summary\n@return " + text).getReturnDescription();
  }

  @Test
  public void inline_html() {
    assertEquals("a bold word", inlined("a <b>bold</b> word"));
    assertEquals("a link", inlined("a <a href=\"https://avaje.io\">link</a>"));
    assertEquals("a < b", inlined("a < b"));
  }

  @Test
  public void inline_code() {
    assertEquals("use foo()", inlined("use {@code foo()}"));
    assertEquals("a < b", inlined("{@code a < b}"));
    assertEquals("List<String>", inlined("{@code List<String>}"));
    assertEquals("new int[]{1, 2}", inlined("{@code new int[]{1, 2}}"));
    assertEquals("<T>", inlined("{@literal <T>}"));
    assertEquals("@Get", inlined("{@code @Get}"));
  }

  @Test
  public void inline_link() {
    assertEquals("see Foo", inlined("see {@link Foo}"));
    assertEquals("see Foo#bar()", inlined("see {@link Foo#bar()}"));
    assertEquals("see the bar", inlined("see {@link Foo#bar() the bar}"));
    assertEquals("see bar", inlined("see {@linkplain Foo#bar() bar}"));
    assertEquals("see Foo#bar(String, int)", inlined("see {@link Foo#bar(String, int)}"));
    assertEquals("see the bar", inlined("see {@link Foo#bar(String, int) the bar}"));
    assertEquals("see nested x", inlined("see {@link Foo nested {@code x}}"));
  }

  @Test
  public void inline_other() {
    assertEquals("max Foo#MAX", inlined("max {@value Foo#MAX}"));
    assertEquals("inherited", inlined("{@inheritDoc}inherited"));
    assertEquals("unclosed code", inlined("unclosed {@code code"));
    assertEquals("plain {braces}", inlined("plain {braces}"));
  }
}
