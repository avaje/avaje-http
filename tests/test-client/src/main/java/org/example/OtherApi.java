package org.example;

import io.avaje.http.api.Get;
import io.avaje.http.api.Header;

import java.time.LocalTime;
import java.util.UUID;

public interface OtherApi {

  @Get("{uid}")
  Repo get(UUID uid, Boolean bazz, LocalTime tm, @Header String xPath);
}
