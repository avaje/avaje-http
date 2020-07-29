package org.example.myapp.web;

import io.dinject.controller.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class BarController implements BarInterface {

  @Override
  public Bar getById(long id) {
    return new Bar();
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
