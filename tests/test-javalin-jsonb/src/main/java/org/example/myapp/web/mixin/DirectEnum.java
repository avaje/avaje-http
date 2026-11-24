package org.example.myapp.web.mixin;

import io.avaje.jsonb.Json;

public enum DirectEnum {
  VISIBLE,
  @Json.Ignore
  HIDDEN,
  @Json.Ignore(deserialize = true)
  INPUT_ONLY
}
