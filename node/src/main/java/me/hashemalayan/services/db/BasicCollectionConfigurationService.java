package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.protobuf.util.Timestamps;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import me.hashemalayan.services.db.exceptions.CollectionConfigurationNotFoundException;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.InvalidCollectionSchemaException;
import me.hashemalayan.services.db.interfaces.CollectionConfigurationService;
import me.hashemalayan.services.db.models.CollectionConfiguration;
import me.hashemalayan.util.Constants;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BasicCollectionConfigurationService implements CollectionConfigurationService {

    private final NodeProperties nodeProperties;
    private final Logger logger;
    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory jsonSchemaFactory;
    private final Map<String, CollectionConfiguration> configurationMap;
    private final Path collectionsPath;

    private final JsonSchema metaSchema;

    @Inject
    public BasicCollectionConfigurationService(
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

    @Override
    public void load() {

        try {
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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void save(Path filePath, CollectionConfiguration config) {

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), config);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public CollectionConfiguration createMetaData(String collectionName, String schema) {

        try {
            for (final var config : configurationMap.values()) {
                final var name = config.getMetaData().getName();
                if (collectionName.equals(name) && !config.getMetaData().getDeleted()) {
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

            if (!isValidRootSchema(mappedSchema)) {
                throw new InvalidCollectionSchemaException("Root schema should be an object");
            }

            final var jsonSchema = jsonSchemaFactory.getSchema(mappedSchema);
            final var config = new CollectionConfiguration(metaData, jsonSchema);
            final var collectionPath = collectionsPath.resolve(metaData.getId());

            if (!Files.exists(collectionPath))
                Files.createDirectories(collectionPath);

            final var configFilePath = collectionPath.resolve("config.json");

            save(configFilePath, config);

            configurationMap.put(metaData.getId(), config);

            return config;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean isValidRootSchema(JsonNode schema) {

        if (schema.has("type")) {
            JsonNode typeNode = schema.get("type");
            if (typeNode.isTextual()) {
                return "object".equals(typeNode.asText());
            } else if (typeNode.isArray()) {
                for (JsonNode typeValue : typeNode) {
                    if (!"object".equals(typeValue.asText())) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<CollectionMetaData> getCollectionMetaData(String collectionId) {

        if (!configurationMap.containsKey(collectionId))
            return Optional.empty();

        return Optional.of(configurationMap.get(collectionId).getMetaData());
    }

    @Override
    public Optional<JsonSchema> getCollectionSchema(String collectionId) {

        if (!configurationMap.containsKey(collectionId))
            return Optional.empty();

        return Optional.of(configurationMap.get(collectionId).getSchema());
    }

    @Override
    public List<CollectionMetaData> getAllCollectionsMetaData() {

        return configurationMap.values()
                .stream()
                .map(CollectionConfiguration::getMetaData)
                .filter(x -> !x.getDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public boolean collectionConfigurationIsLoaded(String collectionId) {
        return configurationMap.containsKey(collectionId);
    }

    @Override
    public Set<String> validateAgainstMetaSchema(JsonNode schema) {
        return metaSchema.validate(schema).stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toSet());
    }

    @Override
    public void editCollection(String collectionId, String collectionName) {

        if (!configurationMap.containsKey(collectionId))
            throw new CollectionDoesNotExistException();

        final var currentMetaData = configurationMap.get(collectionId).getMetaData();

        configurationMap.get(collectionId).setMetaData(
                currentMetaData.toBuilder()
                        .setName(collectionName != null ? collectionName : currentMetaData.getName())
                        .build()
        );

        final var configFilePath = collectionsPath.resolve(collectionId).resolve("config.json");
        save(configFilePath, configurationMap.get(collectionId));
    }

    @Override
    public void deleteCollection(String collectionId) {
        editCollectionMetaData(collectionId, (metadata) -> metadata.setDeleted(true));
    }

    @Override
    public void editCollectionMetaData(
            String collectionId,
            Function<CollectionMetaData.Builder, CollectionMetaData.Builder> editor
    ) {

        if (!configurationMap.containsKey(collectionId))
            throw new CollectionDoesNotExistException();

        final var currentMetaData = configurationMap.get(collectionId).getMetaData();
        final var builder = currentMetaData.toBuilder();
        final var newMetaData = editor.apply(builder).build();

        configurationMap.get(collectionId).setMetaData(newMetaData);

        final var configFilePath = collectionsPath.resolve(collectionId).resolve("config.json");
        save(configFilePath, configurationMap.get(collectionId));
    }

    @Override
    public void createConfiguration(CollectionMetaData metaData, String schema) {

        try {
            if (configurationMap.containsKey(metaData.getId())) {
                throw new CollectionAlreadyExistsException();
            }

            final var jsonSchema = jsonSchemaFactory.getSchema(schema);
            final var config = new CollectionConfiguration(metaData, jsonSchema);
            final var collectionPath = collectionsPath.resolve(metaData.getId());

            if (!Files.exists(collectionPath))
                Files.createDirectories(collectionPath);

            final var configFilePath = collectionPath.resolve("config.json");

            save(configFilePath, config);
            configurationMap.put(metaData.getId(), config);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
