package me.hashemalayan.db.handlers;

import jakarta.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.db.events.ValidateAllSchemasEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ValidateAllSchemasEventHandler implements EventHandler<ValidateAllSchemasEvent> {

    @Inject
    private NodeProperties nodeProperties;

    @Inject
    private Logger logger;

    @Override
    public void handle(ValidateAllSchemasEvent event) {

        final var storagePath = Paths.get(nodeProperties.getName());
        final var collectionsPath = storagePath.resolve("collections");

        // /nodeX/collections/
        try (final var collectionsStream = Files.newDirectoryStream(collectionsPath)) {

            // /nodeX/collections/collectionX/
            for (final var collectionDirectoryPath : collectionsStream) {
                logger.debug("Validating collection: " + collectionDirectoryPath);

                final var indexesPath = collectionsPath.resolve("indexes");
                final var documentsPath = collectionsPath.resolve("documents");

                try (final var documentsStream = Files.newDirectoryStream(documentsPath)) {
                    for (final var documentPath : documentsStream) {

                    }
                } catch (IOException e) {
                    logger.error("An I/O error occurred");
                }
            }
        } catch (IOException e) {
            logger.error("An I/O error occurred");
        }
    }

    @Override
    public Class<ValidateAllSchemasEvent> getHandledEventType() {
        return ValidateAllSchemasEvent.class;
    }
}
