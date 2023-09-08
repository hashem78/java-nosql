package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.networknt.schema.JsonSchemaException;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionDocument;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.nosql.shared.DocumentMetaData;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.InvalidCollectionSchemaException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public class CollectionService {

    private final CollectionConfigurationService configService;
    private final Path collectionsPath;
    private final ObjectMapper objectMapper;

    @Inject
    public CollectionService(
            CollectionConfigurationService configService,
            NodeProperties nodeProperties,
            ObjectMapper objectMapper) {
        this.configService = configService;

        collectionsPath = Paths.get(
                nodeProperties.getName(),
                "collections"
        );
        this.objectMapper = objectMapper;
    }

    public CollectionMetaData createCollection(String collectionName, String schema)
            throws IOException,
            CollectionAlreadyExistsException,
            InvalidCollectionSchemaException {

        var collectionPath = collectionsPath.resolve(collectionName);

        if (Files.exists(collectionPath))
            throw new CollectionAlreadyExistsException();


        return configService.createMetaData(collectionName, schema);
    }

    public List<CollectionMetaData> getCollections() {

        return configService.getAllCollectionsMetaData();
    }

    public void getDocuments(String collectionName, Consumer<CollectionDocument> onDocumentLoaded)
            throws CollectionDoesNotExistException, IOException {

        final var collectionPath = collectionsPath.resolve(collectionName);
        final var collectionExistsOnDisk = Files.exists(collectionPath);
        final var configIsLoaded = configService.collectionConfigurationIsLoaded(collectionName);

        if (!collectionExistsOnDisk || !configIsLoaded)
            throw new CollectionDoesNotExistException();

        final var documentsPath = collectionPath.resolve("documents");

        try (final var documentPathStream = Files.newDirectoryStream(documentsPath)) {

            for (final var documentPath : documentPathStream) {
                if (!Files.isRegularFile(documentPath))
                    continue;

                final var documentJson = objectMapper.readTree(documentPath.toFile());

                final var metaData = objectMapper.treeToValue(
                        documentJson.get("metaData"),
                        DocumentMetaData.class
                );
                final var dataString = objectMapper.writeValueAsString(documentJson.get("data"));

                onDocumentLoaded.accept(
                        CollectionDocument.newBuilder()
                                .setMetaData(metaData)
                                .setData(dataString)
                                .build()
                );
            }
        }
    }
}
