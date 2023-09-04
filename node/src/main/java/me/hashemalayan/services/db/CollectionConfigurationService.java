package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.Timestamps;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import jakarta.inject.Inject;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.services.db.exceptions.CollectionConfigurationNotFoundException;
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

    private final Path collectionPath;

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
        collectionPath = Paths.get(
                nodeProperties.getName(),
                "collections"
        );
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

                    configurationMap.put(configuration.getMetaData().getName(), configuration);

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

    CollectionMetaData createMetaData(String collectionName) throws IOException {

        final var metaData = CollectionMetaData.newBuilder()
                .setName(collectionName)
                .setId(UUID.randomUUID().toString())
                .setCreatedOn(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();

        final var config = new CollectionConfiguration(metaData, null);
        final var configFilePath = collectionPath.resolve(collectionName).resolve("config.json");

        save(configFilePath, config);

        configurationMap.put(collectionName, config);

        return metaData;
    }

    public void setCollectionSchema(String collectionName, JsonNode schemaNode) throws IOException {

        final var schema = jsonSchemaFactory.getSchema(schemaNode);
        final var config = configurationMap.get(collectionName);
        config.setSchema(schema);

        final var configFilePath = collectionPath.resolve(collectionName).resolve("config.json");
        save(configFilePath, config);
    }

    Optional<CollectionMetaData> getCollectionMetaData(String collectionName) {

        if (!configurationMap.containsKey(collectionName))
            return Optional.empty();

        return Optional.of(configurationMap.get(collectionName).getMetaData());
    }

    Optional<JsonSchema> getCollectionSchema(String collectionName) {

        if (!configurationMap.containsKey(collectionName))
            return Optional.empty();

        final var schema = configurationMap.get(collectionName).getSchema();

        if (schema == null) return Optional.empty();

        return Optional.of(schema);
    }

    List<CollectionMetaData> getAllCollectionsMetaData() {

        return configurationMap.values()
                .stream()
                .map(CollectionConfiguration::getMetaData)
                .collect(Collectors.toList());
    }


}
