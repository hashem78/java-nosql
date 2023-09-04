package me.hashemalayan.services.db;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.CollectionMetaData;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DatabaseService {
    private final CollectionsMetaDataService collectionsMetaDataService;
    private final Path collectionsPath;

    @Inject
    public DatabaseService(
            CollectionsMetaDataService collectionsMetaDataService,
            NodeProperties nodeProperties
    ) {
        this.collectionsMetaDataService = collectionsMetaDataService;

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

        return collectionsMetaDataService.addMetaData(collectionName);
    }

    public List<CollectionMetaData> getCollections() {

        return collectionsMetaDataService.getAllCollectionsMetaData();
    }
}
