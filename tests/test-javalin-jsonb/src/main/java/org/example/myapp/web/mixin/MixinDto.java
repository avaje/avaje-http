package org.example.myapp.web.mixin;

import io.avaje.jsonb.Json;

@Json
public class MixinDto {

  public External data;

  public ExternalEnum externalEnum;

  public DirectEnum directEnum;

  @Json.Ignore
  public String hidden;
}
