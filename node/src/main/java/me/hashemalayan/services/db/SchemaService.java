package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.services.interfaces.SchemaLoader;
import me.hashemalayan.factories.JsonDirectoryIteratorFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class SchemaService {
    private Map<String, JsonSchema> schemaMap;
    private final NodeProperties nodeProperties;
    private final Logger logger;
    private final JsonSchemaFactory jsonSchemaFactory;
    private final SchemaLoader schemaLoader;

    private final JsonDirectoryIteratorFactory jsonDirectoryIteratorFactory;

    @Inject
    public SchemaService(
            NodeProperties nodeProperties,
            Logger logger,
            JsonSchemaFactory jsonSchemaFactory,
            SchemaLoader schemaLoader,
            JsonDirectoryIteratorFactory jsonDirectoryIteratorFactory
    ) {
        this.nodeProperties = nodeProperties;
        this.logger = logger;
        this.jsonSchemaFactory = jsonSchemaFactory;
        this.schemaLoader = schemaLoader;
        this.jsonDirectoryIteratorFactory = jsonDirectoryIteratorFactory;
    }

    public void load() {
        schemaMap = schemaLoader.load();
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

    public void validateAll() {
        final var storagePath = Paths.get(nodeProperties.getName());
        final var collectionsPath = storagePath.resolve("collections");

        // /nodeX/collections/
        try (final var collectionsStream = Files.newDirectoryStream(collectionsPath)) {

            // /nodeX/collections/collectionX/
            for (final var collectionDirectoryPath : collectionsStream) {

                logger.debug("Validating collection: " + collectionDirectoryPath);

                final var documentsPath = collectionDirectoryPath.resolve("documents");
                final var documentsIterator = jsonDirectoryIteratorFactory.create(documentsPath);

                for (var iterationResult : documentsIterator) {
                    final var errors = validateDocument(
                            collectionDirectoryPath.getFileName().toString(),
                            iterationResult.jsonNode()
                    );
                    if (errors.isEmpty()) {
                        logger.debug("Successfully validated " + iterationResult.documentName());
                    } else {
                        logger.error("Failed to validate schema for document: " + iterationResult.documentName() + " in " + documentsPath);
                        for (final var error : errors) {
                            logger.error(error.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("An I/O error occurred");
            e.printStackTrace();
        }
    }
}
