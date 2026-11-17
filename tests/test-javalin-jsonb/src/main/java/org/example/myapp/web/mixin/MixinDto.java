package org.example.myapp.web.mixin;

import io.avaje.jsonb.Json;

@Json
public class MixinDto {

  public External data;

  @Json.Ignore
  public String hidden;
}
