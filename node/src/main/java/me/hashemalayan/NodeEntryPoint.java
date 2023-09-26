package me.hashemalayan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.protobuf.util.Timestamps;
import me.hashemalayan.nosql.shared.Common;
import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.services.db.exceptions.UncheckedBTreeException;
import me.hashemalayan.services.db.interfaces.CollectionService;
import me.hashemalayan.services.db.interfaces.IndexService;
import me.hashemalayan.services.db.interfaces.SchemaService;
import me.hashemalayan.services.grpc.interfaces.LocalServicesManager;
import me.hashemalayan.services.grpc.interfaces.RemoteSignalingService;
import me.hashemalayan.util.Constants;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.hashemalayan.nosql.shared.Common.CollectionMetaData;

public class NodeEntryPoint {

    final private LocalServicesManager nodeManager;
    final private RemoteSignalingService remoteSignalingService;
    final private SchemaService schemaService;
    final private IndexService indexService;
    final private CollectionService collectionService;
    final private NodeProperties nodeProperties;
    final private ObjectMapper objectMapper;
    final private Logger logger;

    @Inject
    public NodeEntryPoint(
            LocalServicesManager nodeManager,
            RemoteSignalingService remoteSignalingService,
            SchemaService schemaService,
            IndexService indexService,
            CollectionService collectionService,
            NodeProperties nodeProperties,
            ObjectMapper objectMapper, Logger logger) {
        this.nodeManager = nodeManager;
        this.remoteSignalingService = remoteSignalingService;
        this.schemaService = schemaService;
        this.indexService = indexService;
        this.collectionService = collectionService;
        this.nodeProperties = nodeProperties;
        this.objectMapper = objectMapper;
        this.logger = logger;
    }

    void run() {
        try {
            logger.info("Initializing NodeManager");
            nodeManager.init();
            logger.info("Initializing SignalingClient");
            remoteSignalingService.init();
            logger.info("Initializing LocalDatabase");
            initializeLocalDatabase();
            logger.info("Loading Schemas to memory");
            schemaService.load();
            logger.info("Validating all Schemas");
            schemaService.validateAll();
            logger.info("Loading indexes");
            indexService.load();
            logger.info("Initializing Auth");
            initializeAuth();
            nodeManager.awaitTermination();
        } catch (IOException | InterruptedException | UncheckedBTreeException e) {
            throw new RuntimeException(e);
        }
    }

    void initializeAuth() {
        final var authMetaData = collectionService.getCollectionMetaData("auth");
        if (authMetaData.isPresent()) return;

        try {
            final var createdOn = Timestamps.fromMillis(0);

            collectionService.createCollection(
                    CollectionMetaData.newBuilder()
                            .setId("auth")
                            .setName("auth")
                            .setDeleted(false)
                            .addAllIndexedProperties(new ArrayList<>())
                            .setCreatedOn(createdOn)
                            .build(),
                    Constants.authSchema
            );
            final var userId = "admin";
            final var documentId = "admin";
            final var userJsonNode = objectMapper.createObjectNode();

            userJsonNode.put("userId", userId);
            userJsonNode.put("email", "admin@db.com");
            userJsonNode.put("password", "123456");

            collectionService.setDocument(
                    "auth",
                    CollectionDocument
                            .newBuilder()
                            .setMetaData(
                                    Common.DocumentMetaData.newBuilder()
                                            .setCreatedOn(createdOn)
                                            .setLastEditedOn(createdOn)
                                            .setAffinity(8001)
                                            .setId(documentId)
                                            .setDeleted(false)
                                            .build()
                            )
                            .setData(objectMapper.writeValueAsString(userJsonNode))
                            .build()
            );

            indexService.indexPropertyInCollection("auth", "name");
            indexService.indexPropertyInCollection("auth", "email");
            indexService.indexPropertyInCollection("auth", "password");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    void initializeLocalDatabase() {
        final var nodeName = nodeProperties.getName();
        final var rootPath = Paths.get(nodeName);

        if (Files.exists(rootPath)) {

            if (!Files.isDirectory(rootPath)) {
                logger.error("Storage exists but it's not a directory.");
                System.exit(1);
            }

            final var collectionsPath = rootPath.resolve("collections");
            if (Files.exists(collectionsPath)) {
                if (!Files.isDirectory(collectionsPath)) {
                    logger.error("Collections exists but it's not a directory.");
                    System.exit(1);
                }

            } else {
                createDirectory(collectionsPath);
            }
        } else {
            createDirectory(rootPath);
        }
    }

    private void createDirectory(Path path) {
        try {
            Files.createDirectories(path);
            logger.debug("Directory " + path + " was created successfully");
        } catch (IOException e) {
            logger.error("Failed to create directory " + path);
            System.exit(1);
        }
    }
}
