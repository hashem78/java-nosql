package me.hashemalayan.services.db.interfaces;

import btree4j.BTreeException;
import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;
import me.hashemalayan.services.db.models.CollectionConfiguration;
import me.hashemalayan.services.db.exceptions.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface CollectionService {
    CollectionConfiguration createCollection(String collectionName, String schema)
            throws IOException,
            InvalidCollectionSchemaException,
            CollectionAlreadyExistsException;

    List<CollectionMetaData> getCollections();

    void getDocuments(String collectionId, Consumer<CollectionDocument> onDocumentLoaded)
            throws CollectionDoesNotExistException, IOException;

    CollectionDocument getDocument(String collectionId, String documentId)
            throws CollectionDoesNotExistException,
            IOException,
            DocumentDoesNotExistException;

    CollectionDocument setDocument(
            String collectionId,
            String documentId,
            String documentJson
    ) throws IOException,
            CollectionDoesNotExistException,
            DocumentSchemaValidationException,
            BTreeException,
            IndexNotFoundException,
            AffinityMismatchException;

    void setDocument(String collectionId, CollectionDocument document)
            throws CollectionDoesNotExistException,
            IOException,
            DocumentSchemaValidationException,
            BTreeException,
            IndexNotFoundException;

    void editCollection(String collectionId, String collectionName)
            throws CollectionDoesNotExistException,
            IOException;

    void deleteCollection(String collectionId)
            throws CollectionDoesNotExistException, IOException;

    void deleteDocument(String collectionId, String documentId)
            throws CollectionDoesNotExistException,
            DocumentDoesNotExistException, IOException;

    Optional<CollectionMetaData> getCollectionMetaData(String collectionId);

    void createCollection(CollectionMetaData metaData, String schema)
            throws IOException,
            CollectionAlreadyExistsException;
}
