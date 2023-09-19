package me.hashemalayan.services.db;

import com.google.inject.Inject;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;

public class DatabaseService extends AbstractDatabaseService {

    @Inject
    public DatabaseService(
            CollectionService collectionService,
            SchemaService schemaService,
            IndexService indexService
    ) {
        super(collectionService, schemaService, indexService);
    }
}
