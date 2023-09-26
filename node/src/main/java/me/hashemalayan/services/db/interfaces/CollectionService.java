package me.hashemalayan.services.db.interfaces;

import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;
import me.hashemalayan.services.db.exceptions.*;
import me.hashemalayan.services.db.models.CollectionConfiguration;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface CollectionService {
    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws InvalidCollectionSchemaException if the collection schema is invalid.
     * @throws CollectionAlreadyExistsException if the collection already exists.
     */
    CollectionConfiguration createCollection(String collectionName, String schema);

    List<CollectionMetaData> getCollections();

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    void getDocuments(String collectionId, Consumer<CollectionDocument> onDocumentLoaded);

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     * @throws DocumentDoesNotExistException if the document does not exist.
     */
    CollectionDocument getDocument(String collectionId, String documentId);

    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws DocumentSchemaValidationException if the document schema validation fails.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws IndexNotFoundException if the index is not found.
     * @throws AffinityMismatchException if there's an affinity mismatch.
     * @throws DocumentOptimisticLockException if there's an optimistic lock exception on the document.
     */
    CollectionDocument setDocument(String collectionId, String documentId, String documentJson);

    /**
     * @throws CollectionDoesNotExistException if the collection does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     * @throws DocumentSchemaValidationException if the document schema validation fails.
     * @throws UncheckedBTreeException if there's an error with the BTree.
     * @throws IndexNotFoundException if the index is not found.
     * @throws DocumentOptimisticLockException if there's an optimistic lock exception on the document.
     */
    void setDocument(String collectionId, CollectionDocument document);

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
     * @throws DocumentDoesNotExistException if the document does not exist.
     * @throws UncheckedIOException in case of I/O issues.
     */
    void deleteDocument(String collectionId, String documentId);

    Optional<CollectionMetaData> getCollectionMetaData(String collectionId);

    /**
     * @throws UncheckedIOException in case of I/O issues.
     * @throws CollectionAlreadyExistsException if the collection already exists.
     */
    void createCollection(CollectionMetaData metaData, String schema);
}
