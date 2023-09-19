package me.hashemalayan.services.db.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import me.hashemalayan.nosql.shared.Common;
import me.hashemalayan.services.db.CollectionConfiguration;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.InvalidCollectionSchemaException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface CollectionConfigurationService {
    void load() throws IOException;

    void save(Path filePath, CollectionConfiguration config) throws IOException;

    CollectionConfiguration createMetaData(String collectionName, String schema)
            throws IOException,
            InvalidCollectionSchemaException,
            CollectionAlreadyExistsException;

    boolean isValidRootSchema(JsonNode schema);

    Optional<Common.CollectionMetaData> getCollectionMetaData(String collectionId);

    Optional<JsonSchema> getCollectionSchema(String collectionId);

    List<Common.CollectionMetaData> getAllCollectionsMetaData();

    boolean collectionConfigurationIsLoaded(String collectionId);

    Set<String> validateAgainstMetaSchema(JsonNode schema);

    void editCollection(String collectionId, String collectionName)
            throws CollectionDoesNotExistException,
            IOException;

    void deleteCollection(String collectionId)
            throws CollectionDoesNotExistException,
            IOException;

    void editCollectionMetaData(
            String collectionId,
            Function<Common.CollectionMetaData.Builder, Common.CollectionMetaData.Builder> editor
    ) throws CollectionDoesNotExistException, IOException;

    void createConfiguration(Common.CollectionMetaData metaData, String schema) throws IOException,
            CollectionAlreadyExistsException;
}
