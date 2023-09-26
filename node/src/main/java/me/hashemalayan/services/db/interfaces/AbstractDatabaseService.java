package me.hashemalayan.services.db.interfaces;

import me.hashemalayan.nosql.shared.CollectionPropertyType;
import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;
import me.hashemalayan.nosql.shared.Customstruct.CustomValue;
import me.hashemalayan.nosql.shared.Operator;
import me.hashemalayan.services.db.exceptions.*;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractDatabaseService {
    private final CollectionService collectionService;
    private final SchemaService schemaService;
    private final IndexService indexService;

    protected AbstractDatabaseService(
            CollectionService collectionService,
            SchemaService schemaService,
            IndexService indexService
    ) {
        this.collectionService = collectionService;
        this.schemaService = schemaService;
        this.indexService = indexService;
    }

    public List<CollectionMetaData> getCollections() {
        return collectionService.getCollections();
    }

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    public void getDocuments(String collectionId, Consumer<CollectionDocument> onDocumentLoaded) {
        collectionService.getDocuments(collectionId, onDocumentLoaded);
    }

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    public void editCollection(String collectionId, String collectionName) {
        collectionService.editCollection(collectionId, collectionName);
    }

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    public void deleteCollection(String collectionId) {
        collectionService.deleteCollection(collectionId);
    }

    /**
     * @throws SampleMalformedException if the sample is malformed.
     * @throws CollectionDoesNotExistException if the collection does not exist.
     */
    public String getDocumentSample(String collectionId) {
        return schemaService.getSample(collectionId);
    }

    /**
     * @throws DocumentSchemaValidationException if the document schema validation fails.
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws IndexNotFoundException if the index is not found.
     * @throws AffinityMismatchException if there's an affinity mismatch.
     * @throws DocumentOptimisticLockException if there's an optimistic lock exception on the document.
     */
    public CollectionDocument setDocument(String collectionId, String documentId, String documentJson) {
        return collectionService.setDocument(collectionId, documentId, documentJson);
    }

    /**
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws DocumentSchemaValidationException if the document schema validation fails.
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws IndexNotFoundException if the index is not found.
     * @throws UncheckedIOException in case of I/O issues.
     * @throws DocumentOptimisticLockException if there's an optimistic lock exception on the document.
     */
    public void setDocument(String collectionId, CollectionDocument document) {
        collectionService.setDocument(collectionId, document);
    }

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws DocumentDoesNotExistException if the document does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    public void deleteDocument(String collectionId, String documentId) {
        collectionService.deleteDocument(collectionId, documentId);
    }

    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws CollectionAlreadyExistsException if the collection already exists.
     * @throws InvalidCollectionSchemaException if the collection schema is invalid.
     */
    public CollectionMetaData createCollection(String collectionName, String schema) {
        return collectionService.createCollection(collectionName, schema).getMetaData();
    }

    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws CollectionAlreadyExistsException if the collection already exists.
     */
    public void createCollection(CollectionMetaData collectionMetaData, String schema) {
        collectionService.createCollection(collectionMetaData, schema);
    }

    public Optional<CollectionMetaData> getCollectionMetaData(String collectionId) {
        return collectionService.getCollectionMetaData(collectionId);
    }

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws DocumentDoesNotExistException if the document does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    public CollectionDocument getDocument(String collectionId, String documentId) {
        return collectionService.getDocument(collectionId, documentId);
    }

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws PropertyDoesNotExistException if the property does not exist.
     */
    public CollectionPropertyType getPropertyType(String collectionId, String property) {
        return schemaService.getPropertyType(collectionId, property);
    }

    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws CollectionDoesNotExistException if the collection does not exist.
     */
    public void indexPropertyInCollection(String collectionId, String property) {
        indexService.indexPropertyInCollection(collectionId, property);
    }

    public boolean isPropertyIndexed(String collectionId, String property) {
        return indexService.isPropertyIndexed(collectionId, property);
    }

    /**
     * @throws IndexNotFoundException if the index is not found.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws UncheckedIOException in case of I/O issues.
     * @throws CollectionDoesNotExistException if the collection does not exist.
     */
    public void removeIndexFromCollectionProperty(String collectionId, String property) {
        indexService.removeIndexFromCollectionProperty(collectionId, property);
    }

    /**
     * @throws IndexNotFoundException if the index is not found.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws InvalidOperatorUsage if the operator usage is invalid.
     * @throws UnRecognizedOperatorException if the operator is unrecognized.
     */
    public void runQuery(
            String collectionId,
            Operator operator,
            String property,
            CustomValue value,
            Consumer<String> responseConsumer
    ) {
        indexService.runQuery(collectionId, operator, property, value, responseConsumer);
    }
}
