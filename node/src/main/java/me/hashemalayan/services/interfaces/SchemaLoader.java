package me.hashemalayan.services.interfaces;

import com.networknt.schema.JsonSchema;

import java.util.Map;

public interface SchemaLoader {
    Map<String, JsonSchema> load();
}
