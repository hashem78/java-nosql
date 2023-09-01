package me.hashemalayan.db.handlers;

import jakarta.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.db.SchemaManager;
import me.hashemalayan.db.events.ValidateAllSchemasEvent;
import me.hashemalayan.util.JsonDirectoryIteratorFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ValidateAllSchemasEventHandler implements EventHandler<ValidateAllSchemasEvent> {

    @Inject
    private NodeProperties nodeProperties;

    @Inject
    private Logger logger;

    @Inject
    private SchemaManager schemaManager;

    @Inject
    private JsonDirectoryIteratorFactory jsonDirectoryIteratorFactory;

    @Override
    public void handle(ValidateAllSchemasEvent event) {

        final var storagePath = Paths.get(nodeProperties.getName());
        final var collectionsPath = storagePath.resolve("collections");

        // /nodeX/collections/
        try (final var collectionsStream = Files.newDirectoryStream(collectionsPath)) {

            // /nodeX/collections/collectionX/
            for (final var collectionDirectoryPath : collectionsStream) {

                logger.debug("Validating collection: " + collectionDirectoryPath);

                final var documentsPath = collectionDirectoryPath.resolve("documents");
                final var documentsIterator = jsonDirectoryIteratorFactory.create(documentsPath);

                for (var iterationResult : documentsIterator) {
                    final var errors = schemaManager.validateDocument(
                            collectionDirectoryPath.getFileName().toString(),
                            iterationResult.jsonNode()
                    );
                    if (errors.isEmpty()) {
                        logger.debug("Successfully validated " + iterationResult.documentName());
                    } else {
                        logger.error("Failed to validate schema for document: " + iterationResult.documentName() + " in " + documentsPath);
                        for (final var error : errors) {
                            logger.error(error.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("An I/O error occurred");
            e.printStackTrace();
        }
    }

    @Override
    public Class<ValidateAllSchemasEvent> getHandledEventType() {
        return ValidateAllSchemasEvent.class;
    }
}
