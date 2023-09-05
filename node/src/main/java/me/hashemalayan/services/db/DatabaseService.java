package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionDocument;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.nosql.shared.DocumentMetaData;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DatabaseService {
    private final CollectionConfigurationService configService;
    private final Path collectionsPath;

    private final ObjectMapper objectMapper;

    @Inject
    public DatabaseService(
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

    public CollectionMetaData createCollection(String collectionName)
            throws IOException,
            CollectionAlreadyExistsException {

        var collectionPath = collectionsPath.resolve(collectionName);

        if (Files.exists(collectionPath))
            throw new CollectionAlreadyExistsException();

        return configService.createMetaData(collectionName);
    }

    public List<CollectionMetaData> getCollections() {

        return configService.getAllCollectionsMetaData();
    }

    public void getDocuments(String collectionName, StreamObserver<CollectionDocument> responseObserver)
            throws CollectionDoesNotExistException {

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

                responseObserver.onNext(
                        CollectionDocument.newBuilder()
                                .setMetaData(metaData)
                                .setData(dataString)
                                .build()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
