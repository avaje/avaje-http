module io.avaje.http.api.javalin {
	exports io.avaje.http.lambda;
    exports io.avaje.http.lambda.events;
    exports io.avaje.http.lambda.json;
    requires transitive io.avaje.http.api;
    requires static com.fasterxml.jackson.databind;
    requires static io.avaje.jsonb;

	//Why is there not even an automatic module???
    requires transitive aws.lambda.java.core;
}
