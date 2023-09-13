package me.hashemalayan.services.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import me.hashemalayan.nosql.shared.CollectionDocument;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.nosql.shared.CollectionPropertyType;
import me.hashemalayan.services.db.exceptions.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DatabaseService {

    private final CollectionService collectionService;
    private final SchemaService schemaService;

    @Inject
    public DatabaseService(
            CollectionService collectionService,
            SchemaService schemaService) {
        this.collectionService = collectionService;
        this.schemaService = schemaService;
    }

    public CollectionMetaData createCollection(String collectionName, String schema)
            throws IOException,
            CollectionAlreadyExistsException,
            InvalidCollectionSchemaException {
        return collectionService.createCollection(collectionName, schema);
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
    ) throws DocumentSchemaValidationException, CollectionDoesNotExistException, IOException {
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
}
