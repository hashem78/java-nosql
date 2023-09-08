package me.hashemalayan.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.networknt.schema.JsonSchema;

import java.io.IOException;

public class JsonSchemaSerializer extends JsonSerializer<JsonSchema> {



    @Override
    public void serialize(
            JsonSchema value,
            JsonGenerator gen,
            SerializerProvider serializers
    ) throws IOException {

        gen.writeObject(value.getSchemaNode());
    }
}
