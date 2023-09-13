package me.hashemalayan.services.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.Timestamps;
import com.networknt.schema.ValidationMessage;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionDocument;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.nosql.shared.DocumentMetaData;
import me.hashemalayan.services.db.exceptions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class CollectionService {

    private final CollectionConfigurationService configService;

    private final SchemaService schemaService;
    private final Path collectionsPath;
    private final NodeProperties nodeProperties;
    private final ObjectMapper objectMapper;

    @Inject
    public CollectionService(
            CollectionConfigurationService configService,
            SchemaService schemaService,
            NodeProperties nodeProperties,
            ObjectMapper objectMapper) {
        this.configService = configService;
        this.schemaService = schemaService;

        collectionsPath = Paths.get(
                nodeProperties.getName(),
                "collections"
        );
        this.nodeProperties = nodeProperties;
        this.objectMapper = objectMapper;
    }

    public CollectionMetaData createCollection(String collectionName, String schema)
            throws IOException,
            InvalidCollectionSchemaException,
            CollectionAlreadyExistsException {

        return configService.createMetaData(collectionName, schema);
    }

    public List<CollectionMetaData> getCollections() {

        return configService.getAllCollectionsMetaData();
    }

    public void getDocuments(String collectionId, Consumer<CollectionDocument> onDocumentLoaded)
            throws CollectionDoesNotExistException, IOException {

        final var collectionPath = collectionsPath.resolve(collectionId);
        final var collectionExistsOnDisk = Files.exists(collectionPath);
        final var configIsLoaded = configService.collectionConfigurationIsLoaded(collectionId);

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

                if (metaData.getDeleted()) continue;

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

    public CollectionDocument getDocument(String collectionId, String documentId)
            throws CollectionDoesNotExistException,
            IOException,
            DocumentDoesNotExistException {

        final var collectionPath = collectionsPath.resolve(collectionId);
        final var collectionExistsOnDisk = Files.exists(collectionPath);
        final var configIsLoaded = configService.collectionConfigurationIsLoaded(collectionId);

        if (!collectionExistsOnDisk || !configIsLoaded)
            throw new CollectionDoesNotExistException();

        final var documentsPath = collectionPath.resolve("documents");
        final var documentPath = documentsPath.resolve(documentId + ".json");

        if(!Files.exists(documentPath))
            throw new DocumentDoesNotExistException();

        if (!Files.isRegularFile(documentPath))
            throw new IOException();

        final var documentJson = objectMapper.readTree(documentPath.toFile());

        final var metaData = objectMapper.treeToValue(
                documentJson.get("metaData"),
                DocumentMetaData.class
        );

        final var dataString = objectMapper.writeValueAsString(documentJson.get("data"));

        return CollectionDocument.newBuilder()
                .setMetaData(metaData)
                .setData(dataString)
                .build();
    }

    public CollectionDocument setDocument(
            String collectionId,
            String documentId,
            String documentJson
    ) throws IOException,
            CollectionDoesNotExistException, DocumentSchemaValidationException {

        String actualDocumentId = documentId;

        if (actualDocumentId.isEmpty())
            actualDocumentId = UUID.randomUUID().toString();

        if (!Files.exists(collectionsPath.resolve(collectionId))) {
            throw new CollectionDoesNotExistException();
        }

        final var documentsPath = collectionsPath.resolve(collectionId)
                .resolve("documents");

        if (!Files.exists(documentsPath))
            Files.createDirectories(documentsPath);

        final var documentPath = documentsPath
                .resolve(actualDocumentId + ".json");

        DocumentMetaData metaData = null;

        final var timeStamp = Timestamps.fromMillis(System.currentTimeMillis());
        if (Files.exists(documentPath)) {
            final var diskDocumentJson = objectMapper.readTree(documentPath.toFile());
            metaData = objectMapper.treeToValue(
                            diskDocumentJson.get("metaData"),
                            DocumentMetaData.class
                    ).toBuilder()
                    .setLastEditedOn(timeStamp)
                    .build();
        } else {

            metaData = DocumentMetaData.newBuilder()
                    .setAffinity(nodeProperties.getPort())
                    .setId(actualDocumentId)
                    .setCreatedOn(timeStamp)
                    .setLastEditedOn(timeStamp)
                    .build();
        }

        final var dataNode = objectMapper.readTree(documentJson);
        final var metaDataNode = objectMapper.readTree(JsonFormat.printer().print(metaData));

        final var documentNode = objectMapper.createObjectNode();
        documentNode.set("data", dataNode);
        documentNode.set("metaData", metaDataNode);

        final var report = schemaService.validateDocument(
                collectionId,
                documentNode
        );

        if (!report.isEmpty()) {
            throw new DocumentSchemaValidationException(
                    report.stream()
                            .map(ValidationMessage::getMessage)
                            .reduce(String::concat)
                            .orElse("")
            );
        }

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        documentPath.toFile(),
                        documentNode
                );

        return CollectionDocument
                .newBuilder()
                .setMetaData(metaData)
                .setData(
                        objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(dataNode)
                )
                .build();
    }

    public void editCollection(String collectionId, String collectionName)
            throws CollectionDoesNotExistException,
            IOException {
        configService.editCollection(collectionId, collectionName);
    }

    public void deleteCollection(String collectionId) throws CollectionDoesNotExistException, IOException {
        configService.deleteCollection(collectionId);
    }

    public void deleteDocument(String collectionId, String documentId)
            throws CollectionDoesNotExistException,
            DocumentDoesNotExistException, IOException {

        if (!Files.exists(collectionsPath.resolve(collectionId))) {
            throw new CollectionDoesNotExistException();
        }

        final var documentPath = collectionsPath.resolve(collectionId)
                .resolve("documents")
                .resolve(documentId + ".json");

        if (!Files.exists(documentPath)) {
            throw new DocumentDoesNotExistException();
        }

        final var timeStamp = Timestamps.fromMillis(System.currentTimeMillis());

        final var documentNode = objectMapper.readTree(Files.readString(documentPath));

        final var metaDataNode = (ObjectNode) documentNode.get("metaData");
        metaDataNode.put("lastEditedOn", Timestamps.toString(timeStamp));
        metaDataNode.put("deleted", true);

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        documentPath.toFile(),
                        documentNode
                );
    }

    public Optional<CollectionMetaData> getCollectionMetaData(String collectionId) {
        return configService.getCollectionMetaData(collectionId);
    }
}
