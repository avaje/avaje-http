package io.avaje.http.generator.client.clients;

import io.avaje.http.api.Cookie;
import io.avaje.http.api.Header;
import io.avaje.http.api.QueryParam;

public class Params {

  public String param;

  @QueryParam("named")
  private String param2;

  @Header private String head;
  @Cookie private String cook;
  public String publicQuery;

  public String getParam() {
    return param;
  }

  public void setParam(String param) {
    this.param = param;
  }

  public String getParam2() {
    return param2;
  }

  public void setParam2(String param2) {
    this.param2 = param2;
  }

  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public String getCook() {
    return cook;
  }

  public void setCook(String cook) {
    this.cook = cook;
  }
}
