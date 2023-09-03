package me.hashemalayan;

import com.google.inject.Inject;
import me.hashemalayan.db.SchemaManager;
import me.hashemalayan.server.LocalNodeManager;
import me.hashemalayan.signaling.SignalingClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NodeEntryPoint {

    final private LocalNodeManager nodeManager;
    final private SignalingClient signalingClient;

    final private SchemaManager schemaManager;

    final private NodeProperties nodeProperties;

    final private Logger logger;
    @Inject
    public NodeEntryPoint(
            LocalNodeManager nodeManager,
            SignalingClient signalingClient,
            SchemaManager schemaManager,
            NodeProperties nodeProperties,
            Logger logger) {
        this.nodeManager = nodeManager;
        this.signalingClient = signalingClient;
        this.schemaManager = schemaManager;
        this.nodeProperties = nodeProperties;
        this.logger = logger;
    }

    void run() {
        try {
            logger.info("Initializing NodeManager");
            nodeManager.init();
            logger.info("Initializing SignalingClient");
            signalingClient.init();
            logger.info("Initializing LocalDatabase");
            initializeLocalDatabase();
            logger.info("Loading Schemas to memory");
            schemaManager.load();
            logger.info("Validating all Schemas");
            schemaManager.validateAll();
            nodeManager.awaitTermination();
        } catch (IOException | InterruptedException e) {
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
