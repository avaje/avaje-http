package org.example.server;

import io.avaje.http.api.Controller;
import org.example.CommonApi;

import java.time.LocalDate;

@Controller
public class CommonController implements CommonApi {

  @Override
  public String hello() {
    return "hello world";
  }

  @Override
  public String name(String name) {
    return "name[" + name + "]";
  }

  @Override
  public String p2(long id, String name, LocalDate after, Boolean more) {
    return "p2[" + id + ";" + name + "; after:" + after + " more:" + more + "]";
  }
}
