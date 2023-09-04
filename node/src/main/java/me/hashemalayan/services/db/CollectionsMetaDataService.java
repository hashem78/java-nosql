package me.hashemalayan.services.db;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.Timestamps;
import jakarta.inject.Inject;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionsMetaDataService {

    private final NodeProperties nodeProperties;
    private final Logger logger;
    private final Map<String, CollectionMetaData> metaDataMap;

    @Inject
    public CollectionsMetaDataService(
            NodeProperties nodeProperties,
            Logger logger
    ) {
        this.nodeProperties = nodeProperties;
        this.logger = logger;
        this.metaDataMap = new ConcurrentHashMap<>();
    }

    public void loadAllCollectionsMetaData() throws IOException {

        logger.info("Loading metadata for all collections");
        final var collectionsPath = Paths.get(nodeProperties.getName(), "collections");

        if (!Files.exists(collectionsPath))
            Files.createDirectories(collectionsPath);

        try (final var directoryStream = Files.walk(collectionsPath)) {


            final var collectionPaths = directoryStream
                    .filter(Files::isDirectory)
                    .toList();


            for (final var path : collectionPaths) {

                logger.info("Loading schema for " + path.getFileName());

                try {

                    final var schemaString = Files.readString(path.resolve("metadata.json"));
                    final var collectionMetaDataBuilder = CollectionMetaData.newBuilder();
                    JsonFormat.parser().merge(schemaString, collectionMetaDataBuilder);
                    final var collectionMetaData = collectionMetaDataBuilder.build();
                    metaDataMap.put(path.getFileName().toString(), collectionMetaData);

                } catch (IOException e) {

                    logger.error("FATAL: Failed to find schema for " + path.getFileName());
                    System.exit(1);

                }
            }
        }
    }

    CollectionMetaData addMetaData(String collectionName) throws IOException {

        final var collectionPath = Paths.get(
                nodeProperties.getName(),
                "collections",
                collectionName
        );

        final var metaData = CollectionMetaData.newBuilder()
                .setName(collectionName)
                .setId(UUID.randomUUID().toString())
                .setCreatedOn(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();

        final var metaDataJson = JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
                .print(metaData);

        Files.writeString(
                Files.createDirectories(collectionPath).resolve("metaData.json"),
                metaDataJson
        );

        return metaData;
    }

    Optional<CollectionMetaData> getCollectionMetaData(String collectionName) {

        if (!metaDataMap.containsKey(collectionName))
            return Optional.empty();

        return Optional.of(metaDataMap.get(collectionName));
    }

    List<CollectionMetaData> getAllCollectionsMetaData() {

        return new ArrayList<>(metaDataMap.values());
    }
}
