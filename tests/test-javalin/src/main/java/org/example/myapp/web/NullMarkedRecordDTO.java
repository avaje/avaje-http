package org.example.myapp.web;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record NullMarkedRecordDTO(
  String notNullable,
  @Nullable String nullable
) {
}
