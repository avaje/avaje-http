package org.example.myapp.web;

import io.dinject.controller.Controller;

import java.util.ArrayList;
import java.util.List;

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
  public String barMessage() {
    return "Hello";
  }
}
