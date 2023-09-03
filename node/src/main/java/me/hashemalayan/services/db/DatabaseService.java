package me.hashemalayan.services.db;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.Timestamps;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class DatabaseService {

    private final NodeProperties nodeProperties;
    private final Logger logger;

    @Inject
    public DatabaseService(
            NodeProperties nodeProperties,
            Logger logger
    ) {
        this.nodeProperties = nodeProperties;
        this.logger = logger;
    }

    public CollectionMetaData createCollection(String collectionName)
            throws InvalidProtocolBufferException,
            CollectionAlreadyExistsException {

        var metaData = CollectionMetaData.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setCreatedOn(Timestamps.fromMillis(System.currentTimeMillis()))
                .build();

        var metaDataJson = JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames()
                .print(metaData);

        var collectionPath = Paths.get(
                nodeProperties.getName(),
                "collections",
                collectionName
        );

        if (Files.exists(collectionPath))
            throw new CollectionAlreadyExistsException();

        try {

            Files.writeString(
                    Files.createDirectories(collectionPath).resolve("metaData.json"),
                    metaDataJson
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return metaData;
    }
}
