package org.example.web.myapp.service;

import java.util.ArrayList;
import java.util.List;

import org.example.web.myapp.WebHelloDto;

import jakarta.inject.Singleton;

@Singleton
public class MyService {

  public List<WebHelloDto> findAll() {

    List<WebHelloDto> list = new ArrayList<>();
    list.add(new WebHelloDto(12, "Jim", "asd"));
    list.add(new WebHelloDto(13, "Spock", "456456"));

    return list;
  }
}
