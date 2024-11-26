package org.example.web.myapp.service;

import java.util.ArrayList;
import java.util.List;

import org.example.web.myapp.HelloDto;

import jakarta.inject.Singleton;

@Singleton
public class MyService {

  public List<HelloDto> findAll() {

    List<HelloDto> list = new ArrayList<>();
    list.add(new HelloDto(12, "Jim", "asd"));
    list.add(new HelloDto(13, "Spock", "456456"));

    return list;
  }
}
