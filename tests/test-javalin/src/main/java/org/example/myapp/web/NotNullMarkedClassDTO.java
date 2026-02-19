package org.example.myapp.web;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class NotNullMarkedClassDTO {
  String string1;
  @Nullable String string2;

  String[] stringArray1;
  @Nullable String[] stringArray2;

  Set<String> set1;
  Set<@Nullable String> set2;
  @Nullable Set<String> set3;

  Map<String, String> map1;
  Map<@Nullable String, String> map2; // In version 3.0 you cannot set the key so testing this is difficult.
  Map<String, @Nullable String> map3;
  @Nullable Map<String, String> map4;
}
