package io.avaje.http.client;

import io.avaje.json.simple.SimpleMapper;

/**
 * A BodyAdapter that supports converting the request/response body to a single type.
 * <p>
 * Useful for an endpoint that only returns a single JSON response type.
 */
public interface SingleBodyAdapter extends BodyAdapter {

    /**
     * Create with an json content adapter for a single java type.
     *
     * @param jsonAdapter The adapter to use to read and write the body content.
     * @return The BodyAdapter that the HttpClient can use.
     */
    static BodyAdapter create(JsonBodyAdapter<?> jsonAdapter) {
        return DSingleAdapter.of(jsonAdapter);
    }

    /**
     * Create using an avaje-json-core simple mapper type.
     *
     * @param jsonType The only type supported to read or write the body content.
     * @return The BodyAdapter that the HttpClient can use.
     */
    static BodyAdapter create(SimpleMapper.Type<?> jsonType) {
        return DSingleAdapter.of(jsonType);
    }

    /**
     * Json body reading and writing for a single type.
     *
     * @param <T> The Java type of the request or response body.
     */
    interface JsonBodyAdapter<T> {

        /**
         * Read the raw content String and return the java type.
         */
        T fromJsonString(String json);

        /**
         * Read the raw content bytes and return the java type.
         */
        T fromJsonBytes(byte[] bytes);

        /**
         * Write the java type to bytes.
         */
        byte[] toJsonBytes(T bean);
    }
}
