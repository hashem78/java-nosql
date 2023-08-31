package me.hashemalayan.db;

import com.github.erosb.jsonsKema.JsonValue;

import java.util.Map;

record CollectionSchema<T>(String collectionName, T schema) {
}


interface DBSchemaLoader {
    Map<String, CollectionSchema<JsonValue>> load();
}

public class SchemaManager {
    Map<String, CollectionSchema<JsonValue>> schemaMap;

    SchemaManager(DBSchemaLoader loader) {
        this.schemaMap = loader.load();
    }
}
