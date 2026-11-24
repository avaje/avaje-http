package org.example.myapp.web.mixin;

import io.avaje.jsonb.Json;

@Json.MixIn(ExternalEnum.class)
public enum ExternalEnumMixin {
  @Json.Ignore
  BAD_CONSTANT
}
