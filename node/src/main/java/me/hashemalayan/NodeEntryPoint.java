package me.hashemalayan;

import btree4j.BTreeException;
import com.google.inject.Inject;
import me.hashemalayan.services.db.exceptions.UncheckedBTreeException;
import me.hashemalayan.services.db.interfaces.IndexService;
import me.hashemalayan.services.db.interfaces.SchemaService;
import me.hashemalayan.services.grpc.interfaces.LocalServicesManager;
import me.hashemalayan.services.grpc.interfaces.RemoteSignalingService;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NodeEntryPoint {

    final private LocalServicesManager nodeManager;
    final private RemoteSignalingService remoteSignalingService;
    final private SchemaService schemaService;
    final private IndexService indexService;
    final private NodeProperties nodeProperties;

    final private Logger logger;
    @Inject
    public NodeEntryPoint(
            LocalServicesManager nodeManager,
            RemoteSignalingService remoteSignalingService,
            SchemaService schemaService,
            IndexService indexService,
            NodeProperties nodeProperties,
            Logger logger) {
        this.nodeManager = nodeManager;
        this.remoteSignalingService = remoteSignalingService;
        this.schemaService = schemaService;
        this.indexService = indexService;
        this.nodeProperties = nodeProperties;
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
            nodeManager.awaitTermination();
        } catch (IOException | InterruptedException | UncheckedBTreeException e) {
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
