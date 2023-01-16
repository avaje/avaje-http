package org.example;

import io.avaje.http.api.Header;
import io.avaje.http.api.QueryParam;

public class CommonParams {

  public Long firstRow;
  public Long maxRows;
  public String sortBy;
  @QueryParam("X-Xtr")
  private String extra;
  @Header
  public String filter;

  public Long firstRow() {
    return firstRow;
  }

  public String getExtra() {
    return extra;
  }
}
