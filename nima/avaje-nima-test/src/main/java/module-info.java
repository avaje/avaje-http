import io.avaje.nima.test.NimaTestPlugin;

module io.avaje.nima.test {

  exports io.avaje.nima.test;

  requires transitive io.avaje.nima;
  requires transitive io.avaje.http.client;
  requires transitive io.avaje.inject.test;

  requires static io.avaje.jsonb;
  requires static com.fasterxml.jackson.databind;
  requires static org.apiguardian.api; // stink man !!

  provides io.avaje.inject.test.Plugin with NimaTestPlugin;
}
