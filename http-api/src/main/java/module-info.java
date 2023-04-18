module io.avaje.http.api {

	exports io.avaje.http.api;
	exports io.avaje.http.api.context;
	requires static io.avaje.inject;

	provides io.avaje.inject.spi.Plugin with io.avaje.http.api.spi.DefaultResolverProvider;

}
