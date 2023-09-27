package me.hashemalayan.services.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.Timestamps;
import com.networknt.schema.ValidationMessage;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;
import me.hashemalayan.nosql.shared.Common.DocumentMetaData;
import me.hashemalayan.services.db.exceptions.*;
import me.hashemalayan.services.db.interfaces.CollectionConfigurationService;
import me.hashemalayan.services.db.interfaces.CollectionService;
import me.hashemalayan.services.db.interfaces.IndexService;
import me.hashemalayan.services.db.interfaces.SchemaService;
import me.hashemalayan.services.db.models.CollectionConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class BasicCollectionService implements CollectionService {

    private final CollectionConfigurationService configService;
    private final SchemaService schemaService;
    private final IndexService indexService;
    private final Path collectionsPath;
    private final NodeProperties nodeProperties;
    private final ObjectMapper objectMapper;

    @Inject
    public BasicCollectionService(
            CollectionConfigurationService configService,
            SchemaService schemaService,
            IndexService indexService,
            NodeProperties nodeProperties,
            ObjectMapper objectMapper) {
        this.configService = configService;
        this.schemaService = schemaService;
        this.indexService = indexService;

        collectionsPath = Paths.get(
                nodeProperties.getName(),
                "collections"
        );
        this.nodeProperties = nodeProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public CollectionConfiguration createCollection(String collectionName, String schema) {
        return configService.createMetaData(collectionName, schema);
    }

    public List<CollectionMetaData> getCollections() {
        return configService.getAllCollectionsMetaData();
    }

    public void getDocuments(String collectionId, Consumer<CollectionDocument> onDocumentLoaded) {

        try {
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public CollectionDocument getDocument(String collectionId, String documentId) {

        try {
            final var collectionPath = collectionsPath.resolve(collectionId);
            final var collectionExistsOnDisk = Files.exists(collectionPath);
            final var configIsLoaded = configService.collectionConfigurationIsLoaded(collectionId);

            if (!collectionExistsOnDisk || !configIsLoaded)
                throw new CollectionDoesNotExistException();

            final var documentsPath = collectionPath.resolve("documents");
            final var documentPath = documentsPath.resolve(documentId + ".json");

            if (!Files.exists(documentPath))
                throw new DocumentDoesNotExistException();

            if (!Files.isRegularFile(documentPath))
                throw new UncheckedIOException(new IOException());

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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public CollectionDocument setDocument(
            String collectionId,
            String documentId,
            String documentJson
    ) {

        try {
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

            DocumentMetaData newMetaData;

            final var timeStamp = Timestamps.fromMillis(System.currentTimeMillis());
            DocumentMetaData oldMetaData = null;
            JsonNode oldDataNode = null;
            if (Files.exists(documentPath)) {
                final var diskDocumentJson = objectMapper.readTree(documentPath.toFile());
                oldDataNode = diskDocumentJson.get("data");
                oldMetaData = objectMapper.treeToValue(
                        diskDocumentJson.get("metaData"),
                        DocumentMetaData.class
                );
                newMetaData = oldMetaData.toBuilder()
                        .setLastEditedOn(timeStamp)
                        .build();
            } else {

                newMetaData = DocumentMetaData.newBuilder()
                        .setAffinity(nodeProperties.getPort())
                        .setId(actualDocumentId)
                        .setCreatedOn(timeStamp)
                        .setLastEditedOn(timeStamp)
                        .build();
            }

            if (newMetaData.getAffinity() != nodeProperties.getPort())
                throw new AffinityMismatchException(newMetaData.getAffinity());

            final var dataNode = objectMapper.readTree(documentJson);
            final var metaDataNode = objectMapper.readTree(JsonFormat.printer().print(newMetaData));


            final var documentNode = objectMapper.createObjectNode();
            documentNode.set("data", dataNode);
            documentNode.set("metaData", metaDataNode);


            validateDocument(collectionId, documentNode);
            indexDocument(collectionId, actualDocumentId, documentNode, oldDataNode);

            // Re-read the document from disk and check if it's been changed before
            // this change has finished
            if (oldMetaData != null)
                guardDocument(documentPath, oldMetaData);

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(
                            documentPath.toFile(),
                            documentNode
                    );

            return CollectionDocument
                    .newBuilder()
                    .setMetaData(newMetaData)
                    .setData(
                            objectMapper.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(dataNode)
                    )
                    .build();
        } catch (InvalidProtocolBufferException | JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void guardDocument(Path documentPath, DocumentMetaData metaData) {
        try {

            final var diskDocumentJson = objectMapper.readTree(documentPath.toFile());
            final var diskLastEditedOn = Timestamps.parse(
                    diskDocumentJson.get("metaData").get("lastEditedOn").textValue()
            );
            int comparisonResult = Timestamps.compare(diskLastEditedOn, metaData.getLastEditedOn());
            if (comparisonResult != 0) {
                throw new DocumentOptimisticLockException();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setDocument(String collectionId, CollectionDocument document) {

        try {
            final var documentId = document.getMetaData().getId();

            if (!Files.exists(collectionsPath.resolve(collectionId))) {
                throw new CollectionDoesNotExistException();
            }

            final var documentsPath = collectionsPath.resolve(collectionId)
                    .resolve("documents");

            if (!Files.exists(documentsPath))
                Files.createDirectories(documentsPath);

            final var documentPath = documentsPath
                    .resolve(documentId + ".json");

            JsonNode oldDataNode = null;
            DocumentMetaData oldMetaData = null;
            if (Files.exists(documentPath)) {
                final var diskDocumentJson = objectMapper.readTree(documentPath.toFile());
                oldMetaData = objectMapper.treeToValue(
                        diskDocumentJson.get("metaData"),
                        DocumentMetaData.class
                );
                oldDataNode = diskDocumentJson.get("data");
            }

            final var documentNode = objectMapper.createObjectNode();
            final var metaDataNode = objectMapper.readTree(JsonFormat.printer().print(document.getMetaData()));
            final var dataNode = objectMapper.readTree(document.getData());

            documentNode.set("data", dataNode);
            documentNode.set("metaData", metaDataNode);

            validateDocument(collectionId, documentNode);
            indexDocument(collectionId, documentId, documentNode, oldDataNode);

            // Re-read the document from disk and check if it's been changed before
            // this change has finished
            if (oldMetaData != null)
                guardDocument(documentPath, oldMetaData);

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(
                            documentPath.toFile(),
                            documentNode
                    );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void validateDocument(String collectionId, JsonNode documentNode) {
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
    }

    private void indexDocument(
            String collectionId,
            String documentId,
            ObjectNode documentNode,
            JsonNode oldDataNode
    ) {
        try {
            final var dataNode = documentNode.get("data");
            final var indexedPropertiesInCollection = indexService.getIndexedProperties(collectionId);
            final var compoundIndexedPropertiesInCollection = indexService.getCompoundIndexedProperties(collectionId);
            for (final var indexedPropertyInCollection : indexedPropertiesInCollection) {
                final var dataNodeToBeAddedToIndex = dataNode.get(indexedPropertyInCollection);
                if (oldDataNode == null) {
                    indexService.addToIndex(
                            collectionId,
                            documentId,
                            indexedPropertyInCollection,
                            objectMapper.writeValueAsBytes(dataNodeToBeAddedToIndex)
                    );
                } else {
                    indexService.addToIndex(
                            collectionId,
                            documentId,
                            indexedPropertyInCollection,
                            objectMapper.writeValueAsBytes(oldDataNode.get(indexedPropertyInCollection)),
                            objectMapper.writeValueAsBytes(dataNodeToBeAddedToIndex)
                    );
                }
            }
            for (final var indexedPropertyInCollection : compoundIndexedPropertiesInCollection) {
                final var properties = indexedPropertyInCollection.split("_");
                final var dataNodeToBeAddedToIndex = objectMapper.createObjectNode();
                for (final var property : properties) {
                    dataNodeToBeAddedToIndex.set(property, dataNode.get(property));
                }
                if (oldDataNode == null) {
                    indexService.addToIndex(
                            collectionId,
                            documentId,
                            indexedPropertyInCollection,
                            objectMapper.writeValueAsBytes(dataNodeToBeAddedToIndex)
                    );
                } else {
                    final var oldKeyNode = objectMapper.createObjectNode();
                    for (final var property : properties) {
                        oldKeyNode.set(property, oldDataNode.get(property));
                    }
                    indexService.addToIndex(
                            collectionId,
                            documentId,
                            indexedPropertyInCollection,
                            objectMapper.writeValueAsBytes(oldKeyNode),
                            objectMapper.writeValueAsBytes(dataNodeToBeAddedToIndex)
                    );
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void editCollection(String collectionId, String collectionName)
            throws CollectionDoesNotExistException,
            UncheckedIOException {
        configService.editCollection(collectionId, collectionName);
    }

    public void deleteCollection(String collectionId) throws CollectionDoesNotExistException, UncheckedIOException {
        configService.deleteCollection(collectionId);
    }

    public void deleteDocument(String collectionId, String documentId) {

        try {
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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    public Optional<CollectionMetaData> getCollectionMetaData(String collectionId) {
        return configService.getCollectionMetaData(collectionId);
    }

    public void createCollection(CollectionMetaData metaData, String schema)
            throws UncheckedIOException,
            CollectionAlreadyExistsException {
        configService.createConfiguration(metaData, schema);
    }
}
