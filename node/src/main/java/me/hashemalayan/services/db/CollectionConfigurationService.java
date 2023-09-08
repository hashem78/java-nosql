package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.Timestamps;
import com.networknt.schema.*;
import jakarta.inject.Inject;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import me.hashemalayan.services.db.exceptions.CollectionConfigurationNotFoundException;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.InvalidCollectionSchemaException;
import me.hashemalayan.util.Constants;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CollectionConfigurationService {

    private final NodeProperties nodeProperties;
    private final Logger logger;
    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory jsonSchemaFactory;
    private final Map<String, CollectionConfiguration> configurationMap;
    private final Path collectionsPath;

    private final JsonSchema metaSchema;

    @Inject
    public CollectionConfigurationService(
            NodeProperties nodeProperties,
            Logger logger,
            ObjectMapper objectMapper,
            JsonSchemaFactory jsonSchemaFactory) {
        this.nodeProperties = nodeProperties;
        this.logger = logger;
        this.objectMapper = objectMapper;
        this.jsonSchemaFactory = jsonSchemaFactory;
        this.configurationMap = new ConcurrentHashMap<>();
        collectionsPath = Paths.get(
                nodeProperties.getName(),
                "collections"
        );
        metaSchema = jsonSchemaFactory.getSchema(Constants.Draft7MetaScheme);
    }

    public void load() throws IOException {

        logger.info("Loading configurations for all collections");
        final var collectionsPath = Paths.get(nodeProperties.getName(), "collections");

        logger.info("Collections Path: " + collectionsPath);

        if (!Files.exists(collectionsPath))
            Files.createDirectories(collectionsPath);

        try (final var directoryStream = Files.newDirectoryStream(collectionsPath)) {

            for (final var path : directoryStream) {

                logger.info("Loading configuration for " + path.getFileName());

                try {

                    final var configFilePath = path.resolve("config.json");

                    logger.info("Config file path: " + configFilePath);

                    if (!Files.exists(configFilePath))
                        throw new CollectionConfigurationNotFoundException();

                    final var configuration = objectMapper.readValue(
                            configFilePath.toFile(),
                            CollectionConfiguration.class
                    );

                    configurationMap.put(configuration.getMetaData().getId(), configuration);

                    logger.info("Loaded configuration for collection " + path.getFileName());

                } catch (CollectionConfigurationNotFoundException e) {

                    logger.error("FATAL: Failed to find config for " + path.getFileName());
                    System.exit(1);
                }
            }
        }
    }

    private void save(Path filePath, CollectionConfiguration config) throws IOException {

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(filePath.toFile(), config);
    }

    CollectionMetaData createMetaData(String collectionName, String schema)
            throws IOException,
            InvalidCollectionSchemaException,
            CollectionAlreadyExistsException {

        for (final var config : configurationMap.values()) {
            final var name = config.getMetaData().getName();
            if (collectionName.equals(name)) {
                throw new CollectionAlreadyExistsException();
            }
        }

        final var metaData = CollectionMetaData.newBuilder()
                .setName(collectionName)
                .setId(UUID.randomUUID().toString())
                .setCreatedOn(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();

        final var mappedSchema = objectMapper.readTree(schema);
        final var report = validateAgainstMetaSchema(mappedSchema);

        if (!report.isEmpty()) {
            throw new InvalidCollectionSchemaException(
                    report.stream()
                            .reduce(String::concat)
                            .orElse("")
            );
        }

        final var jsonSchema = jsonSchemaFactory.getSchema(mappedSchema);
        final var config = new CollectionConfiguration(metaData, jsonSchema);
        final var collectionPath = collectionsPath.resolve(metaData.getId());

        if (!Files.exists(collectionPath))
            Files.createDirectories(collectionPath);

        final var configFilePath = collectionPath.resolve("config.json");

        save(configFilePath, config);

        configurationMap.put(metaData.getId(), config);

        return metaData;
    }

    Optional<CollectionMetaData> getCollectionMetaData(String collectionId) {

        if (!configurationMap.containsKey(collectionId))
            return Optional.empty();

        return Optional.of(configurationMap.get(collectionId).getMetaData());
    }

    Optional<JsonSchema> getCollectionSchema(String collectionId) {

        if (!configurationMap.containsKey(collectionId))
            return Optional.empty();

        return Optional.of(configurationMap.get(collectionId).getSchema());
    }

    List<CollectionMetaData> getAllCollectionsMetaData() {

        return configurationMap.values()
                .stream()
                .map(CollectionConfiguration::getMetaData)
                .filter(x -> !x.getDeleted())
                .collect(Collectors.toList());
    }

    boolean collectionConfigurationIsLoaded(String collectionId) {
        return configurationMap.containsKey(collectionId);
    }

    private Set<String> validateAgainstMetaSchema(JsonNode schema) {
        return metaSchema.validate(schema).stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toSet());
    }

    public void editCollection(String collectionId, String collectionName)
            throws CollectionDoesNotExistException,
            IOException {

        if(!configurationMap.containsKey(collectionId))
            throw new CollectionDoesNotExistException();

        final var currentMetaData = configurationMap.get(collectionId).metaData;

        configurationMap.get(collectionId).setMetaData(
                currentMetaData.toBuilder()
                        .setName(collectionName != null ? collectionName : currentMetaData.getName())
                        .build()
        );

        final var configFilePath = collectionsPath.resolve(collectionId).resolve("config.json");
        save(configFilePath, configurationMap.get(collectionId));
    }

    public void deleteCollection(String collectionId)
            throws CollectionDoesNotExistException,
            IOException {

        if(!configurationMap.containsKey(collectionId))
            throw new CollectionDoesNotExistException();

        final var currentMetaData = configurationMap.get(collectionId).metaData;

        configurationMap.get(collectionId).setMetaData(
                currentMetaData.toBuilder()
                        .setDeleted(true)
                        .build()
        );

        final var configFilePath = collectionsPath.resolve(collectionId).resolve("config.json");
        save(configFilePath, configurationMap.get(collectionId));
    }
}
