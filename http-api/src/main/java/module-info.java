module io.avaje.http.api {

	exports io.avaje.http.api;
	exports io.avaje.http.api.context;
	exports io.avaje.http.api.spi;
	requires static io.avaje.inject;

    //JDK doesn't allow optional serviceloader
	//so inject has it's own ServiceLoader impl that will optionally load

	//provides io.avaje.inject.spi.Plugin with io.avaje.http.api.spi.DefaultResolverProvider;

}
