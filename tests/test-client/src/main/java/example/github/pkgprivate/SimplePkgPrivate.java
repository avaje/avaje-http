package example.github.pkgprivate;

import io.avaje.http.api.Client;
import io.avaje.http.api.Get;
import io.avaje.http.client.HttpException;

@Client
interface SimplePkgPrivate {

  @Get("private")
  String get() throws HttpException;
}
