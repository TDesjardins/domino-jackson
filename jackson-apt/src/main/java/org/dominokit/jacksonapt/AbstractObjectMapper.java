/*
 * Copyright 2013 Nicolas Morel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dominokit.jacksonapt;

import org.dominokit.jacksonapt.deser.array.ArrayJsonDeserializer;
import org.dominokit.jacksonapt.exception.JsonDeserializationException;
import org.dominokit.jacksonapt.exception.JsonSerializationException;
import org.dominokit.jacksonapt.stream.JsonReader;
import org.dominokit.jacksonapt.stream.JsonToken;
import org.dominokit.jacksonapt.stream.JsonWriter;

/**
 * Base implementation of {@link org.dominokit.jacksonapt.ObjectMapper}. It delegates the serialization/deserialization to a serializer/deserializer.
 *
 * @author Nicolas Morel
 * @version $Id: $
 */
public abstract class AbstractObjectMapper<T> implements ObjectMapper<T> {

    private final String rootName;

    private JsonDeserializer<T> deserializer;

    private JsonSerializer<T> serializer;

    /**
     * <p>Constructor for AbstractObjectMapper.</p>
     *
     * @param rootName a {@link java.lang.String} object.
     */
    protected AbstractObjectMapper(String rootName) {
        this.rootName = rootName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T read(String in) throws JsonDeserializationException {
        return read(in, DefaultJsonDeserializationContext.builder().build());
    }

    /**
     * {@inheritDoc}
     */
    public T read(String in, JsonDeserializationContext ctx) throws JsonDeserializationException {
        JsonReader reader = ctx.newJsonReader(in);

        try {

            if (ctx.isUnwrapRootValue()) {

                if (JsonToken.BEGIN_OBJECT != reader.peek()) {
                    throw ctx.traceError("Unwrap root value is enabled but the input is not a JSON Object", reader);
                }
                reader.beginObject();
                if (JsonToken.END_OBJECT == reader.peek()) {
                    throw ctx.traceError("Unwrap root value is enabled but the JSON Object is empty", reader);
                }
                String name = reader.nextName();
                if (!name.equals(rootName)) {
                    throw ctx.traceError("Unwrap root value is enabled but the name '" + name + "' don't match the expected rootName " +
                            "'" + rootName + "'", reader);
                }
                T result = getDeserializer().deserialize(reader, ctx);
                reader.endObject();
                return result;

            } else {

                return getDeserializer().deserialize(reader, ctx);

            }

        } catch (JsonDeserializationException e) {
            // already logged, we just throw it
            throw e;
        } catch (RuntimeException e) {
            throw ctx.traceError(e, reader);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T[] readArray(String input, ArrayJsonDeserializer.ArrayCreator<T> arrayCreator) throws JsonDeserializationException {
        return readArray(input, DefaultJsonDeserializationContext.builder().build(), arrayCreator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T[] readArray(String input, JsonDeserializationContext ctx, ArrayJsonDeserializer.ArrayCreator<T> arrayCreator) throws JsonDeserializationException {
        ArrayJsonDeserializer<T> jsonDeserializer = ArrayJsonDeserializer.newInstance(getDeserializer(), arrayCreator);
        return jsonDeserializer.deserialize(ctx.newJsonReader(input), ctx);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Getter for the field <code>deserializer</code>.</p>
     */
    @Override
    public JsonDeserializer<T> getDeserializer() {
        if (null == deserializer) {
            deserializer = newDeserializer();
        }
        return deserializer;
    }

    /**
     * Instantiates a new deserializer
     *
     * @return a new deserializer
     */
    protected abstract JsonDeserializer<T> newDeserializer();

    /**
     * {@inheritDoc}
     */
    @Override
    public String write(T value) throws JsonSerializationException {
        return write(value, DefaultJsonSerializationContext.builder().build());
    }

    /**
     * {@inheritDoc}
     */
    public String write(T value, JsonSerializationContext ctx) throws JsonSerializationException {
        JsonWriter writer = ctx.newJsonWriter();
        try {
            if (ctx.isWrapRootValue()) {
                writer.beginObject();
                writer.name(rootName);
                getSerializer().serialize(writer, value, ctx);
                writer.endObject();
            } else {
                getSerializer().serialize(writer, value, ctx);
            }
            return writer.getOutput();
        } catch (JsonSerializationException e) {
            // already logged, we just throw it
            throw e;
        } catch (RuntimeException e) {
            throw ctx.traceError(value, e, writer);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Getter for the field <code>serializer</code>.</p>
     */
    @Override
    public JsonSerializer<T> getSerializer() {
        if (null == serializer) {
            serializer = (JsonSerializer<T>) newSerializer();
        }
        return serializer;
    }

    /**
     * Instantiates a new serializer
     *
     * @return a new serializer
     */
    protected abstract JsonSerializer<?> newSerializer();
}
