package me.hashemalayan.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import me.hashemalayan.NodeProperties;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SchemaManager {
    private Map<String, JsonSchema> schemaMap;
    private final NodeProperties nodeProperties;
    private final Logger logger;
    private final JsonSchemaFactory jsonSchemaFactory;
    private final DBSchemaLoader dbSchemaLoader;

    @Inject
    public SchemaManager(
            NodeProperties nodeProperties,
            Logger logger,
            JsonSchemaFactory jsonSchemaFactory,
            DBSchemaLoader dbSchemaLoader) {
        this.nodeProperties = nodeProperties;
        this.logger = logger;
        this.jsonSchemaFactory = jsonSchemaFactory;
        this.dbSchemaLoader = dbSchemaLoader;
    }

    public void load() {
        schemaMap = dbSchemaLoader.load();
    }

    public void putSchema(String collectionName, JsonNode schemaNode) {

        var schema = jsonSchemaFactory.getSchema(schemaNode);
        schemaMap.put(collectionName, schema);

        final var collectionPath = Paths.get(
                nodeProperties.getName(),
                "collections",
                collectionName,
                "schemas"
        );

        try {

            if (!Files.exists(collectionPath)) {
                Files.createDirectory(collectionPath);
            }

            final var schemaBytes = schema.toString().getBytes();
            Files.write(collectionPath, schemaBytes);
            logger.debug("Saved schema " + schema + " to " + collectionPath);
        } catch (IOException e) {
            logger.error("Failed to write schema " + schema);
        }
    }

    public Set<ValidationMessage> validateDocument(String collectionName, JsonNode jsonNode) {
        final var schema = schemaMap.getOrDefault(
                collectionName,
                jsonSchemaFactory.getSchema("false")
        );
        logger.debug("schema for collection " + collectionName + " is: " + schema);
        return schema.validate(jsonNode);
    }
}
