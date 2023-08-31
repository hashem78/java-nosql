package me.hashemalayan.db.handlers;

import jakarta.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.db.events.InitializeLocalDatabaseEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InitializeLocalDatabaseEventHandler implements EventHandler<InitializeLocalDatabaseEvent> {

    @Inject
    private NodeProperties nodeProperties;

    @Inject
    private Logger logger;

    @Override
    public void handle(InitializeLocalDatabaseEvent event) {
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

    @Override
    public Class<InitializeLocalDatabaseEvent> getHandledEventType() {
        return InitializeLocalDatabaseEvent.class;
    }
}
