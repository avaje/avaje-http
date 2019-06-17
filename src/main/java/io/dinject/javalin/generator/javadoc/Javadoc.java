package io.dinject.javalin.generator.javadoc;


import java.util.Collections;
import java.util.Map;

/**
 * Parsed javadoc.
 */
public class Javadoc {

  static final Javadoc EMPTY = new Javadoc();

  private final String summary;

  private final String description;

  private final Map<String, String> params;

  private final String returnDescription;

  private final boolean deprecated;

  /**
   * Parse and return the Javadoc.
   */
  public static Javadoc parse(String content) {
    return new JavadocParser().parse(content);
  }

  Javadoc(String summary, String description, Map<String, String> params, String returnDescription, boolean deprecated) {
    this.summary = summary;
    this.description = description;
    this.params = params;
    this.returnDescription = returnDescription;
    this.deprecated = deprecated;
  }

  private Javadoc() {
    this.summary = "";
    this.description = "";
    this.returnDescription = "";
    this.params = Collections.emptyMap();
    this.deprecated = false;
  }

  public String getSummary() {
    return summary;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public String getReturnDescription() {
    return returnDescription;
  }

  public boolean isDeprecated() {
    return deprecated;
  }
}
