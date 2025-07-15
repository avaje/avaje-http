package org.example.web.myapp;

import io.avaje.http.api.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Controller
public class BarController implements BarInterface {

  @Override
  public Bar getById(long id) {
    Bar bar = new Bar();
    bar.id = id;
    bar.name = "Rob" + id;
    return bar;
  }

  @Override
  public List<Bar> findByCode(String code) {
    return new ArrayList<>();
  }

  @Override
  public Stream<Bar> findByCodeStream(String code) {
    return Stream.of();
  }

  @Override
  public String barMessage() {
    return "Hello";
  }
}
