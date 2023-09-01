package me.hashemalayan.db;

import com.networknt.schema.JsonSchema;

import java.util.Map;

public interface DBSchemaLoader {
    Map<String, JsonSchema> load();
}
