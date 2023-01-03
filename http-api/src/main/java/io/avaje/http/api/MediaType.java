package io.avaje.http.api;

/**
 * Common media types used by controllers.
 */
public interface MediaType {

  /**
   * A {@code String} constant representing {@value #APPLICATION_XML} media type.
   */
  String APPLICATION_XML = "application/xml";

  /**
   * A {@code String} constant representing {@value #APPLICATION_ATOM_XML} media type.
   */
  String APPLICATION_ATOM_XML = "application/atom+xml";

  /**
   * A {@code String} constant representing {@value #APPLICATION_XHTML_XML} media type.
   */
  String APPLICATION_XHTML_XML = "application/xhtml+xml";

  /**
   * A {@code String} constant representing {@value #APPLICATION_SVG_XML} media type.
   */
  String APPLICATION_SVG_XML = "application/svg+xml";

  /**
   * A {@code String} constant representing {@value #APPLICATION_JSON} media type.
   */
  String APPLICATION_JSON = "application/json";

  /**
   * A {@code String} constant representing {@value #APPLICATION_FORM_URLENCODED} media type.
   */
  String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

  /**
   * A {@code String} constant representing {@value #MULTIPART_FORM_DATA} media type.
   */
  String MULTIPART_FORM_DATA = "multipart/form-data";

  /**
   * A {@code String} constant representing {@value #APPLICATION_OCTET_STREAM} media type.
   */
  String APPLICATION_OCTET_STREAM = "application/octet-stream";

  /**
   * A {@code String} constant representing {@value #TEXT_PLAIN} media type.
   */
  String TEXT_PLAIN = "text/plain";

  /**
   * A {@code String} constant representing {@value #TEXT_XML} media type.
   */
  String TEXT_XML = "text/xml";

  /**
   * A {@code String} constant representing {@value #TEXT_HTML} media type.
   */
  String TEXT_HTML = "text/html";

  /**
   * {@link String} representation of Server sent events media type. ("{@value}").
   */
  String SERVER_SENT_EVENTS = "text/event-stream";

  /**
   * {@link String} representation of {@value #APPLICATION_JSON_PATCH_JSON} media type..
   */
  String APPLICATION_JSON_PATCH_JSON = "application/json-patch+json";
  
	/**
	 * {@link String} representation of {@value #APPLICATION_PDF} media type.
	 */
	String APPLICATION_PDF = "application/pdf";

	/**
	 * {@link String} representation of {@value #IMAGE_GIF} media type.
	 */
	String IMAGE_GIF = "image/gif";

	/**
	 * {@link String} representation of {@value #IMAGE_JPEG} media type.
	 */
	String IMAGE_JPEG = "image/jpeg";

	/**
	 * {@link String} representation of {@value #IMAGE_PNG} media type.
	 */
	String IMAGE_PNG = "image/png";

	/**
	 * {@link String} representation of {@value #MULTIPART_MIXED} media type.
	 */
	String MULTIPART_MIXED = "multipart/mixed";

	/**
	 * {@link String} representation of {@value #MULTIPART_RELATED} media type.
	 */
	String MULTIPART_RELATED = "multipart/related";
  
}
