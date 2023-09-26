package me.hashemalayan.services.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.factories.JsonDirectoryIteratorFactory;
import me.hashemalayan.nosql.shared.CollectionPropertyType;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.PropertyDoesNotExistException;
import me.hashemalayan.services.db.interfaces.CollectionConfigurationService;
import me.hashemalayan.services.db.interfaces.SampleFromSchemaService;
import me.hashemalayan.services.db.interfaces.SchemaService;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class BasicSchemaService implements SchemaService {

    private final CollectionConfigurationService configurationService;
    private final SampleFromSchemaService sampleFromSchemaService;
    private final ObjectMapper objectMapper;
    private final NodeProperties nodeProperties;
    private final Logger logger;
    private final JsonDirectoryIteratorFactory jsonDirectoryIteratorFactory;
    private final JsonSchemaFactory jsonSchemaFactory;

    @Inject
    public BasicSchemaService(
            CollectionConfigurationService configurationService,
            SampleFromSchemaService sampleFromSchemaService,
            ObjectMapper objectMapper,
            NodeProperties nodeProperties,
            Logger logger,
            JsonDirectoryIteratorFactory jsonDirectoryIteratorFactory,
            JsonSchemaFactory jsonSchemaFactory) {
        this.configurationService = configurationService;
        this.sampleFromSchemaService = sampleFromSchemaService;
        this.objectMapper = objectMapper;
        this.nodeProperties = nodeProperties;
        this.logger = logger;
        this.jsonDirectoryIteratorFactory = jsonDirectoryIteratorFactory;
        this.jsonSchemaFactory = jsonSchemaFactory;
    }

    @Override
    public void load() {
        configurationService.load();
    }

    @Override
    public String getSample(String collectionId) {

        try {
            final var schema = configurationService.getCollectionSchema(collectionId);

            if (schema.isEmpty()) throw new CollectionDoesNotExistException();

            final var sample = sampleFromSchemaService.getSample(collectionId, schema.get().getSchemaNode());

            if (sample.has("data"))
                return objectMapper.writeValueAsString(sample.get("data"));

            return objectMapper.writeValueAsString(sample);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<ValidationMessage> validateDocument(String collectionName, JsonNode jsonNode) {

        final var schema = configurationService
                .getCollectionSchema(collectionName)
                .orElse(jsonSchemaFactory.getSchema("true"));

        return schema.validate(jsonNode);
    }

    public Set<ValidationMessage> validateDocument(
            String collectionName,
            String jsonDocument
    )  {

        try {
            return validateDocument(collectionName, objectMapper.readTree(jsonDocument));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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

    public CollectionPropertyType getPropertyType(String collectionId, String property) {

        final var schemaOpt = configurationService.getCollectionSchema(collectionId);
        if (schemaOpt.isEmpty()) throw new CollectionDoesNotExistException();

        final var schema = schemaOpt.get();
        final var schemaNode = schema.getSchemaNode();
        final var propertyNode = schemaNode.get("properties").get(property);

        if (propertyNode == null) throw new PropertyDoesNotExistException();
        final var propertyType = propertyNode.get("type").asText();

        return switch (propertyType) {
            case "string" -> CollectionPropertyType.STRING;
            case "integer" -> CollectionPropertyType.INTEGER;
            case "array" -> CollectionPropertyType.ARRAY;
            default -> CollectionPropertyType.UNRECOGNIZED;
        };
    }
}
