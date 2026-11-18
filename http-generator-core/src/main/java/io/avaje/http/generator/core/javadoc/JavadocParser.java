package io.avaje.http.generator.core.javadoc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

class JavadocParser {

  private String returnDesc = "";

  private final Map<String, String> params = new LinkedHashMap<>();

  private boolean deprecated;

  /** Parse the javadoc. */
  Javadoc parse(String text) {
    if (isEmpty(text)) {
      return Javadoc.EMPTY;
    }
    StringBuilder mainContent = new StringBuilder();
    List<StringBuilder> blocks = splitBlocks(text, mainContent);
    for (StringBuilder block : blocks) {
      processBlock(block.toString());
    }
    return splitMain(mergeLines(inline(mainContent.toString())));
  }

  /** Split the text into the main content and the block tags (without the leading {@code @}). */
  private static List<StringBuilder> splitBlocks(String text, StringBuilder mainContent) {
    List<StringBuilder> blocks = new ArrayList<>();
    StringBuilder current = mainContent;
    boolean lineStart = true;
    int depth = 0;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '\n') {
        lineStart = true;
        current.append(c);
        continue;
      }
      if (lineStart && Character.isWhitespace(c)) {
        current.append(c);
        continue;
      }
      if (lineStart && depth == 0 && c == '@') {
        current = new StringBuilder();
        blocks.add(current);
        lineStart = false;
        continue;
      }
      lineStart = false;
      if (c == '{') {
        depth++;
      } else if (c == '}' && depth > 0) {
        depth--;
      }
      current.append(c);
    }
    return blocks;
  }

  private void processBlock(String block) {
    int ws = indexOfWhitespace(block);
    String tagName = ws < 0 ? block : block.substring(0, ws);
    String content = ws < 0 ? "" : block.substring(ws + 1).trim();
    switch (tagName) {
      case "param":
        int nameEnd = indexOfWhitespace(content);
        if (nameEnd < 0) {
          if (!content.isEmpty()) {
            params.put(content, "");
          }
        } else {
          params.put(
              content.substring(0, nameEnd), mergeLines(inline(content.substring(nameEnd + 1))));
        }
        break;
      case "return":
        returnDesc = mergeLines(inline(content));
        break;
      case "deprecated":
        deprecated = true;
        break;
      default:
        // ignore other block tags such as throws, exception, see, since, author, version
    }
  }

  /** Process inline tags and strip html tags. */
  private static String inline(String text) {
    StringBuilder sb = new StringBuilder(text.length());
    int len = text.length();
    for (int i = 0; i < len; i++) {
      char c = text.charAt(i);
      if (c == '{' && i + 1 < len && text.charAt(i + 1) == '@') {
        int end = closingBrace(text, i);
        String tag = text.substring(i + 2, end);
        int ws = indexOfWhitespace(tag);
        String tagName = ws < 0 ? tag : tag.substring(0, ws);
        String content = ws < 0 ? "" : tag.substring(ws + 1);
        sb.append(inlineTag(tagName, content));
        i = end;
      } else if (c == '<' && isTagStart(text, i + 1)) {
        int end = text.indexOf('>', i);
        if (end < 0) {
          sb.append(c);
        } else {
          i = end;
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static boolean isTagStart(String text, int pos) {
    if (pos >= text.length()) {
      return false;
    }
    char c = text.charAt(pos);
    return Character.isLetter(c) || c == '/' || c == '!';
  }

  private static String inlineTag(String tagName, String content) {
    switch (tagName) {
      case "code":
      case "literal":
        return content.replace("\n", "\\n");
      case "link":
      case "linkplain":
        String ref = content.trim();
        int ws = labelStart(ref);
        return ws < 0 ? ref : inline(ref.substring(ws + 1).trim());
      default:
        return inline(content);
    }
  }

  /**
   * Return the index of the brace closing the inline tag starting at the given position, or the
   * text length when not closed.
   */
  private static int closingBrace(String text, int start) {
    int depth = 0;
    for (int i = start; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '{') {
        depth++;
      } else if (c == '}' && --depth == 0) {
        return i;
      }
    }
    return text.length();
  }

  /**
   * Return the index of the whitespace separating a link reference from its label, ignoring
   * whitespace inside the method parameter list (e.g. {@code Foo#bar(String, int) label}).
   */
  private static int labelStart(String ref) {
    int depth = 0;
    for (int i = 0; i < ref.length(); i++) {
      char c = ref.charAt(i);
      if (c == '(') {
        depth++;
      } else if (c == ')' && depth > 0) {
        depth--;
      } else if (depth == 0 && Character.isWhitespace(c)) {
        return i;
      }
    }
    return -1;
  }

  private static int indexOfWhitespace(String text) {
    for (int i = 0; i < text.length(); i++) {
      if (Character.isWhitespace(text.charAt(i))) {
        return i;
      }
    }
    return -1;
  }

  private Javadoc splitMain(String mainText) {
    String desc = "";
    String summary = mainText;

    int pos = mainText.indexOf('.');
    if (pos > -1) {
      summary = mainText.substring(0, pos);
      desc = mainText.substring(pos + 1).trim();
    }
    return new Javadoc(summary, desc, params, returnDesc, deprecated);
  }

  String mergeLines(String multiline) {
    StringJoiner joiner = new StringJoiner(" ");
    for (String line : multiline.split("\n")) {
      line = line.trim();
      if (!line.isEmpty()) {
        joiner.add(line);
      }
    }
    return joiner.toString();
  }

  private static boolean isEmpty(String text) {
    return text == null || text.isEmpty();
  }
}
