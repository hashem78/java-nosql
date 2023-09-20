package me.hashemalayan.services.db.interfaces;

import btree4j.BTreeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import me.hashemalayan.nosql.shared.CollectionPropertyType;
import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;
import me.hashemalayan.nosql.shared.Customstruct.CustomValue;
import me.hashemalayan.nosql.shared.Operator;
import me.hashemalayan.services.db.exceptions.*;

import java.io.IOException;
import java.text.ParseException;
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

    public void getDocuments(String collectionId, Consumer<CollectionDocument> onDocumentLoaded)
            throws CollectionDoesNotExistException, IOException {
        collectionService.getDocuments(collectionId, onDocumentLoaded);
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

    public CollectionDocument setDocument(
            String collectionId,
            String documentId,
            String documentJson
    ) throws DocumentSchemaValidationException,
            CollectionDoesNotExistException,
            IOException,
            BTreeException,
            IndexNotFoundException,
            AffinityMismatchException,
            ParseException, DocumentOptimisticLockException {

        return collectionService.setDocument(collectionId, documentId, documentJson);
    }

    public void setDocument(String collectionId, CollectionDocument document)
            throws BTreeException,
            DocumentSchemaValidationException,
            CollectionDoesNotExistException,
            IndexNotFoundException,
            IOException,
            DocumentOptimisticLockException,
            ParseException {
        collectionService.setDocument(collectionId, document);
    }


    public void deleteDocument(String collectionId, String documentId) throws
            CollectionDoesNotExistException,
            DocumentDoesNotExistException,
            IOException {
        collectionService.deleteDocument(collectionId, documentId);
    }

    public CollectionMetaData createCollection(String collectionName, String schema)
            throws IOException,
            CollectionAlreadyExistsException,
            InvalidCollectionSchemaException {
        return collectionService.createCollection(collectionName, schema).getMetaData();
    }

    public void createCollection(CollectionMetaData collectionMetaData, String schema)
            throws IOException,
            CollectionAlreadyExistsException {

        collectionService.createCollection(collectionMetaData, schema);
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
            CustomValue value,
            Consumer<String> responseConsumer
    ) throws IndexNotFoundException,
            BTreeException,
            InvalidOperatorUsage,
            UnRecognizedOperatorException {
        indexService.runQuery(collectionId, operator, property, value, responseConsumer);
    }
}
