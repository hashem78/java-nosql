package me.hashemalayan.services.db;

import com.google.inject.Inject;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionDocument;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public class DatabaseService {

    private final CollectionService collectionService;

    @Inject
    public DatabaseService(
            CollectionService collectionService
    ) {
        this.collectionService = collectionService;
    }

    public CollectionMetaData createCollection(String collectionName)
            throws IOException,
            CollectionAlreadyExistsException {
        return collectionService.createCollection(collectionName);
    }

    public List<CollectionMetaData> getCollections() {

        return collectionService.getCollections();
    }

    public void getDocuments(String collectionName, Consumer<CollectionDocument> onDocumentLoaded)
            throws CollectionDoesNotExistException, IOException {
        collectionService.getDocuments(collectionName,onDocumentLoaded);
    }
}
