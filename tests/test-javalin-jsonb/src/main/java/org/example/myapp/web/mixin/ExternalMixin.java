package org.example.myapp.web.mixin;

import java.io.File;

import io.avaje.jsonb.Json;

@Json.MixIn(External.class)
public class ExternalMixin {

  @Json.Ignore
  File bad;

  @Json.Ignore(serialize = true)
  String outputOnly;
}
