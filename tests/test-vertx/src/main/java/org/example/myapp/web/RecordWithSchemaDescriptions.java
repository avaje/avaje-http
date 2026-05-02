package org.example.myapp.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * These are some normal descriptions
 * @param item Not overridden
 */
@Schema(description = "I'm overriding the description", example = "{\"item\": \"Hi example\"}")
public record RecordWithSchemaDescriptions(
  @Schema(description = "Overridden", defaultValue = "This is a default value")
  String item
) {

}
