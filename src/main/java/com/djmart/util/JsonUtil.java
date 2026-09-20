package com.djmart.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe Gson wrapper configured for standard JSON serialization and deserialization.
 */
public final class JsonUtil {

    private static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();

        // Custom Timestamp serializer/deserializer for ISO-8601 format
        builder.registerTypeAdapter(Timestamp.class, new JsonSerializer<Timestamp>() {
            @Override
            public JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(DateTimeFormatter.ISO_INSTANT.format(src.toInstant()));
            }
        });

        builder.registerTypeAdapter(Timestamp.class, new JsonDeserializer<Timestamp>() {
            @Override
            public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                try {
                    Instant instant = Instant.parse(json.getAsString());
                    return Timestamp.from(instant);
                } catch (Exception e) {
                    throw new JsonParseException("Failed to parse Timestamp from ISO-8601 string: " + json.getAsString(), e);
                }
            }
        });

        builder.disableHtmlEscaping();
        GSON = builder.create();
    }

    private JsonUtil() {
        // Prevent instantiation
    }

    public static Gson getGson() {
        return GSON;
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }
}
