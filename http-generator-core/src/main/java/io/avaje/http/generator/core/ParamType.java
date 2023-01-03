package io.avaje.http.generator.core;

public enum ParamType {

  CONTEXT("ctx", "ctx"),
  BODY("body", "body"),
  PATHPARAM("pathParam", "path"),
  FORM("form", "form"),
  BEANPARAM("beanParam", "bean"),
  QUERYPARAM("queryParam", "query"),
  FORMPARAM("formParam", "form"),
  COOKIE("cookie", "cookie"),
  HEADER("header", "header"),
  RESPONSE_HANDLER("notUsed", "notUsed");

  private final String code;
  private final String type;

  ParamType(String code, String type) {
    this.code = code;
    this.type = type;
  }

  public String type() {
    return type;
  }

  @Override
  public String toString() {
    return code;
  }
}
