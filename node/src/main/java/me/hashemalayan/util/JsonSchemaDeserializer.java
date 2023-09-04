package me.hashemalayan.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

import java.io.IOException;

public class JsonSchemaDeserializer extends JsonDeserializer<JsonSchema> {
    @Override
    public JsonSchema deserialize(
            JsonParser p,
            DeserializationContext ctxt
    ) throws IOException, JacksonException {
        JsonNode jsonNode = p.getCodec().readTree(p);
        var schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return schemaFactory.getSchema(jsonNode);
    }
}
