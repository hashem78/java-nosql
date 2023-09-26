package me.hashemalayan.services.db.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import me.hashemalayan.nosql.shared.Common;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.InvalidCollectionSchemaException;
import me.hashemalayan.services.db.models.CollectionConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static me.hashemalayan.nosql.shared.Common.*;

public interface CollectionConfigurationService {
    /**
     * @throws UncheckedIOException in case of I/O issues.
     */
    void load();

    /**
     * @throws UncheckedIOException in case of I/O issues.
     */
    void save(Path filePath, CollectionConfiguration config);

    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws InvalidCollectionSchemaException if the collection schema is invalid.
     * @throws CollectionAlreadyExistsException if the collection already exists.
     */
    CollectionConfiguration createMetaData(String collectionName, String schema);

    boolean isValidRootSchema(JsonNode schema);

    Optional<CollectionMetaData> getCollectionMetaData(String collectionId);

    Optional<JsonSchema> getCollectionSchema(String collectionId);

    List<CollectionMetaData> getAllCollectionsMetaData();

    boolean collectionConfigurationIsLoaded(String collectionId);

    Set<String> validateAgainstMetaSchema(JsonNode schema);

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    void editCollection(String collectionId, String collectionName);

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    void deleteCollection(String collectionId);

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    void editCollectionMetaData(
            String collectionId,
            Function<CollectionMetaData.Builder, CollectionMetaData.Builder> editor
    );

    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws CollectionAlreadyExistsException if the collection already exists.
     */
    void createConfiguration(CollectionMetaData metaData, String schema);
}
