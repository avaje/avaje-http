package org.example.myapp.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This has been replaced with a String... hopefully?
 * @param item And this shouldn't be here at all
 */
@Schema(implementation = String.class)
public record RecordWithSchemaImplementation(
  String item
) {
}
