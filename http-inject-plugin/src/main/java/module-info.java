module io.avaje.http.plugin {

	requires io.avaje.http.api;
    requires io.avaje.inject;
    requires static io.avaje.spi;

	provides io.avaje.inject.spi.InjectExtension with io.avaje.http.inject.DefaultResolverProvider;

}
