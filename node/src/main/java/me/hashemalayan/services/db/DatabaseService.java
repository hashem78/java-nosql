package me.hashemalayan.services.db;

import btree4j.BTreeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;
import me.hashemalayan.services.db.exceptions.*;
import me.hashemalayan.services.grpc.RemoteReplicationService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DatabaseService {

    private final CollectionService collectionService;
    private final SchemaService schemaService;
    private final IndexService indexService;
    private final RemoteReplicationService replicationService;

    @Inject
    public DatabaseService(
            CollectionService collectionService,
            SchemaService schemaService, IndexService indexService,
            RemoteReplicationService replicationService
    ) {
        this.collectionService = collectionService;
        this.schemaService = schemaService;
        this.indexService = indexService;
        this.replicationService = replicationService;
    }

    public CollectionMetaData createCollection(String collectionName, String schema)
            throws IOException,
            CollectionAlreadyExistsException,
            InvalidCollectionSchemaException {
        final var configuration = collectionService.createCollection(collectionName, schema);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setCreateCollectionReplicationMessage(
                                CreateCollectionReplicationMessage.newBuilder()
                                        .setMetaData(configuration.getMetaData())
                                        .setSchema(schema)
                                        .build()
                        )
                        .build()
        );
        return configuration.metaData;
    }

    public void createCollection(CollectionMetaData collectionMetaData, String schema)
            throws IOException,
            CollectionAlreadyExistsException {

        collectionService.createCollection(collectionMetaData, schema);
    }

    public List<CollectionMetaData> getCollections() {

        return collectionService.getCollections();
    }

    public void getDocuments(String collectionId, Consumer<CollectionDocument> onDocumentLoaded)
            throws CollectionDoesNotExistException, IOException {
        collectionService.getDocuments(collectionId, onDocumentLoaded);
    }

    public CollectionDocument setDocument(
            String collectionId,
            String documentId,
            String documentJson
    ) throws DocumentSchemaValidationException,
            CollectionDoesNotExistException,
            IOException,
            BTreeException,
            IndexNotFoundException {
        return collectionService.setDocument(collectionId, documentId, documentJson);
    }

    public void editCollection(
            String collectionId,
            String collectionName
    ) throws CollectionDoesNotExistException,
            IOException {
        collectionService.editCollection(collectionId, collectionName);
    }

    public void deleteCollection(String collectionId) throws CollectionDoesNotExistException, IOException {
        collectionService.deleteCollection(collectionId);
    }

    public String getDocumentSample(String collectionId)
            throws SampleMalformedException,
            CollectionDoesNotExistException,
            JsonProcessingException {
        return schemaService.getSample(collectionId);
    }

    public void deleteDocument(String collectionId, String documentId) throws
            CollectionDoesNotExistException,
            DocumentDoesNotExistException,
            IOException {
        collectionService.deleteDocument(collectionId, documentId);
    }

    public Optional<CollectionMetaData> getCollectionMetaData(String collectionId) {
        return collectionService.getCollectionMetaData(collectionId);
    }

    public CollectionDocument getDocument(String collectionId, String documentId)
            throws CollectionDoesNotExistException,
            DocumentDoesNotExistException,
            IOException {
        return collectionService.getDocument(collectionId, documentId);
    }

    public CollectionPropertyType getPropertyType(String collectionId, String property)
            throws CollectionDoesNotExistException,
            PropertyDoesNotExistException {
        return schemaService.getPropertyType(collectionId, property);
    }

    public void indexPropertyInCollection(String collectionId, String property) throws
            IOException,
            BTreeException,
            CollectionDoesNotExistException {
        indexService.indexPropertyInCollection(collectionId, property);
    }

    public boolean isPropertyIndexed(String collectionId, String property) {
        return indexService.isPropertyIndexed(collectionId, property);
    }

    public void removeIndexFromCollectionProperty(String collectionId, String property)
            throws IndexNotFoundException,
            BTreeException,
            IOException, CollectionDoesNotExistException {
        indexService.removeIndexFromCollectionProperty(collectionId, property);
    }

    public void runQuery(
            String collectionId,
            Operator operator,
            String property,
            Customstruct.CustomValue value,
            Consumer<String> responseConsumer
    ) throws IndexNotFoundException,
            JsonProcessingException,
            BTreeException,
            InvalidOperatorUsage,
            UnRecognizedOperatorException {

        indexService.runQuery(
                collectionId,
                operator,
                property,
                value,
                responseConsumer
        );
    }
}
