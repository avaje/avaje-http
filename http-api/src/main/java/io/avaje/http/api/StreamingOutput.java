package io.avaje.http.api;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An avaje {@link Controller} endpoint is able to use an instance of this interface
 * as a return type.
 *
 * <pre>{@code
 * @Post("some_endpoint")
 * @Produces("application/octet-stream")
 * public StreamingOutput thumbnail(
 *   InputStream inputStream,
 *   @QueryParam(Constants.KEY_SIZE) @Min(1) @Max(Constants.MAX_SIZE) Integer size
 * ) throws IOException {
 *   return (os) -> os.write(new byte[] { 0x01, 0x02, 0x03 });
 * }
 * }</pre>
 */

public interface StreamingOutput {
  void write(OutputStream outputStream) throws IOException;
}
