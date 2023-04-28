module io.avaje.http.plugin {

	requires io.avaje.http.api;
	requires io.avaje.inject;

	provides io.avaje.inject.spi.Plugin with io.avaje.http.inject.DefaultResolverProvider;

}
