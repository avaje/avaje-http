package org.example.myapp.web;

import org.jspecify.annotations.Nullable;

public record NotNullMarkedRecordDTO(
  String notNullable,
  @Nullable String nullable
) {
}
