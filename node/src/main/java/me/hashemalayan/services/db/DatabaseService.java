package me.hashemalayan.services.db;

import com.google.inject.Inject;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DatabaseService {
    private final CollectionConfigurationService collectionConfigurationService;
    private final Path collectionsPath;

    @Inject
    public DatabaseService(
            CollectionConfigurationService collectionConfigurationService,
            NodeProperties nodeProperties
    ) {
        this.collectionConfigurationService = collectionConfigurationService;

        collectionsPath = Paths.get(
                nodeProperties.getName(),
                "collections"
        );
    }

    public CollectionMetaData createCollection(String collectionName)
            throws IOException,
            CollectionAlreadyExistsException {

        var collectionPath = collectionsPath.resolve(collectionName);

        if (Files.exists(collectionPath))
            throw new CollectionAlreadyExistsException();

        return collectionConfigurationService.createMetaData(collectionName);
    }

    public List<CollectionMetaData> getCollections() {

        return collectionConfigurationService.getAllCollectionsMetaData();
    }
}
