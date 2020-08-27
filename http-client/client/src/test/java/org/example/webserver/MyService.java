package org.example.webserver;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MyService {

  public List<HelloDto> findAll() {
    List<HelloDto> list = new ArrayList<>();
    list.add(new HelloDto(12, "Jim", "asd"));
    list.add(new HelloDto(13, "Spock", "456456"));
    return list;
  }
}
