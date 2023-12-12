package io.avaje.http.generator.core.javadoc;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static java.util.Arrays.asList;

class JavadocParser {

  private static final Set<String> IGNORED = new HashSet<>(asList("see", "since", "author", "version", "deprecated", "throws"));

  private static final int TEXT = 1;
  private static final int TAG_START = 2;
  private static final int DOCLET_START = 4;
  private static final int PARAM_NAME = 6;
  private static final int PARAM_DESC = 7;
  private static final int RETURN_DESC = 8;
  private static final int IGNORE = 9;
  private static final int CODE = 10;
  private static final String DEPRECATED = "deprecated";

  private int previousState = TEXT;

  private final StringBuilder currentParam = new StringBuilder();
  private final StringBuilder currentDoclet = new StringBuilder();
  private StringBuilder currentContent;

  private int state = TEXT;

  private String returnDesc = "";

  private final Map<String, String> params = new LinkedHashMap<>();

  private boolean deprecated;

private boolean hasReturn;

  /**
   * Parse the javadoc.
   */
  Javadoc parse(String text) {

    if (isEmpty(text)) {
      return Javadoc.EMPTY;
    }

    StringBuilder mainContent = new StringBuilder();
    currentContent = mainContent;

    for (char c : text.toCharArray()) {

      switch (state) {
        case RETURN_DESC:
          if (c == '\n') {
            processReturnDesc(mainContent);
          }
        case PARAM_DESC:
          if (state == PARAM_DESC) {
            processSetParam();
          }
        case CODE:
          if (state == CODE && c == '}') {
            state = previousState;
            break;
          }
        case TEXT:
          processText(c);
          continue;
        case IGNORE:
          processIgnore(mainContent, c);
          continue;
        case DOCLET_START:
          processDocletStart(c);
          continue;
        case PARAM_NAME:
          processParamName(c);
          continue;
        case TAG_START:
          if (c == '>') {
            state = TEXT;
          }
        default:
      }
    }

    if (hasReturn) {
      returnDesc = mergeLines(currentContent.toString());
    }

    return splitMain(mainContent.toString().trim());
  }

  private void processSetParam() {
    params.put(currentParam.toString(), mergeLines(currentContent.toString().trim()));
  }

  private void processReturnDesc(StringBuilder mainContent) {
    state = TEXT;
    previousState = TEXT;
    returnDesc = currentContent.toString();
    currentContent = new StringBuilder(returnDesc);
  }

  private void processText(char c) {
    switch (c) {
      case '{':
      case '@':
        currentDoclet.delete(0, currentDoclet.length());
        state = DOCLET_START;
        break;
      case '<':
        state = TAG_START;
        break;
      case '\n':
        if (state == CODE) {
          currentContent.append("\\n");
          return;
        }
      default:
        if (c != '}' && c != '>') {
          currentContent.append(c);
        }
        break;
    }
  }

  private void processIgnore(StringBuilder mainContent, char c) {
    if (c == '\n') {
      state = previousState;
      currentContent = mainContent;
    }
  }

  private void processParamName(char c) {
    if (c == ' ') {
      currentContent = new StringBuilder();
      state = PARAM_DESC;
      previousState = PARAM_DESC;
    } else {
      currentParam.append(c);
    }
  }

  private void processDocletStart(char c) {
    if (c == ' ' || c == '\n') {
      state = previousState;
      String docletName = currentDoclet.toString();
      if (IGNORED.contains(docletName)) {
        if (DEPRECATED.equals(docletName)) {
          deprecated = true;
        }
        state = IGNORE;
      } else if ("param".equals(docletName)) {
        currentParam.delete(0, currentParam.length());
        state = PARAM_NAME;
      } else if ("return".equals(docletName)) {
        currentContent = new StringBuilder();
        state = RETURN_DESC;
        previousState = RETURN_DESC;
        hasReturn=true;
      } else if ("@code".equals(docletName)) {
        state = CODE;
      }
    } else {
      currentDoclet.append(c);
    }
  }

  private Javadoc splitMain(String mainText) {

    String desc = "";
    String summary = mainText;

    int pos = mainText.indexOf('.');
    if (pos > -1) {
      summary = mainText.substring(0, pos);
      desc = mergeLines(mainText.substring(pos + 1).trim());
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

  private boolean isEmpty(String text) {
    return text == null || text.isEmpty();
  }
}
