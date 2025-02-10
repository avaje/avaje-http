package io.avaje.http.client;

import io.avaje.http.client.SingleBodyAdapter.JsonBodyAdapter;
import io.avaje.json.mapper.JsonMapper;

import java.util.List;

@SuppressWarnings("unchecked")
final class DSingleAdapter implements BodyAdapter {

    private final ReaderWriter<?> adapter;

    static BodyAdapter of(JsonMapper.Type<?> jsonType) {
        return new DSingleAdapter(toAdapter(jsonType));
    }

    static BodyAdapter of(JsonBodyAdapter<?> source) {
        return new DSingleAdapter(source);
    }

    private DSingleAdapter(JsonBodyAdapter<?> source) {
        this.adapter = new ReaderWriter<>(source);
    }

    private static <T> JsonBodyAdapter<T> toAdapter(JsonMapper.Type<T> jsonType) {
        return new SimpleJsonAdapter<>(jsonType);
    }

    @Override
    public <T> BodyWriter<T> beanWriter(Class<?> aClass) {
        return (BodyWriter<T>) adapter;
    }

    @Override
    public <T> BodyReader<T> beanReader(Class<T> aClass) {
        return (BodyReader<T>) adapter;
    }

    @Override
    public <T> BodyReader<List<T>> listReader(Class<T> aClass) {
        return (BodyReader<List<T>>) adapter;
    }

    private static final class ReaderWriter<T> implements BodyReader<T>, BodyWriter<T> {

        private final JsonBodyAdapter<T> adapter;

        ReaderWriter(JsonBodyAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override
        public T readBody(String content) {
            return adapter.fromJsonString(content);
        }

        @Override
        public T read(BodyContent bodyContent) {
            return adapter.fromJsonBytes(bodyContent.content());
        }

        @Override
        public BodyContent write(T bean, String contentType) {
            return BodyContent.asJson(adapter.toJsonBytes(bean));
        }

        @Override
        public BodyContent write(T bean) {
            return BodyContent.asJson(adapter.toJsonBytes(bean));
        }
    }

    private static final class SimpleJsonAdapter<T> implements JsonBodyAdapter<T> {

        private final JsonMapper.Type<T> jsonType;

        public SimpleJsonAdapter(JsonMapper.Type<T> jsonType) {
            this.jsonType = jsonType;
        }

        @Override
        public T fromJsonString(String json) {
            return jsonType.fromJson(json);
        }

        @Override
        public T fromJsonBytes(byte[] bytes) {
            return jsonType.fromJson(bytes);
        }

        @Override
        public byte[] toJsonBytes(T bean) {
            return jsonType.toJsonBytes(bean);
        }
    }
}

