package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.services.interfaces.SchemaLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SchemaLoaderService implements SchemaLoader {

    private final NodeProperties nodeProperties;
    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory jsonSchemaFactory;
    private final Logger logger;

    @Inject
    public SchemaLoaderService(
            NodeProperties nodeProperties,
            ObjectMapper objectMapper,
            JsonSchemaFactory jsonSchemaFactory,
            Logger logger
    ) {
        this.nodeProperties = nodeProperties;
        this.objectMapper = objectMapper;
        this.jsonSchemaFactory = jsonSchemaFactory;
        this.logger = logger;
    }

    @Override
    public Map<String, JsonSchema> load() {
        final var map = new HashMap<String, JsonSchema>();
        final var storagePath = Paths.get(nodeProperties.getName());
        final var collectionsPath = storagePath.resolve("collections");

        if (!Files.exists(collectionsPath)) {
            try {
                Files.createDirectory(collectionsPath);
            } catch (IOException e) {
                logger.error("Failed to create collections directory for " + nodeProperties.getName());
                e.printStackTrace();
                System.exit(1);
            }
        }

        // /nodeX/collections/
        try (final var collectionsStream = Files.newDirectoryStream(collectionsPath)) {

            for (final var collectionPath : collectionsStream) {
                final var schemaFilePath = collectionPath.resolve("schema.json");
                if (!Files.exists(schemaFilePath)) {
                    logger.error("Schema for collection " + collectionPath.getFileName() + " is not present");
                    continue;
                }
                final var jsonNode = objectMapper.readTree(Files.readAllBytes(schemaFilePath));
                final var schema = jsonSchemaFactory.getSchema(jsonNode);
                map.put(collectionPath.getFileName().toString(), schema);
                logger.debug("Loaded schema for " + collectionPath.getFileName());
            }
        } catch (IOException e) {
            logger.error("An I/O error occurred");

        }
        return map;
    }
}
