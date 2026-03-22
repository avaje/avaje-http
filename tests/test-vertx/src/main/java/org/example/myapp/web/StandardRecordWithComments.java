package org.example.myapp.web;

/**
 * This is a standard record with comments, for OpenAPI to generate from
 * @param blah The first param
 * @param anotherBlah The second param
 */
public record StandardRecordWithComments(
  String blah,
  int anotherBlah
) {
}
