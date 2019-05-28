package io.dinject.javalin.generator;

enum ParamType {

  FORM("form"),
  BEANPARAM("beanParam"),
  QUERYPARAM("queryParam"),
  FORMPARAM("formParam"),
  COOKIE("cookie"),
  HEADER("header");

  private String code;

  ParamType(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }
}
