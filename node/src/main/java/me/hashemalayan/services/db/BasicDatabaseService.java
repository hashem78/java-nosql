package me.hashemalayan.services.db;

import com.google.inject.Inject;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;
import me.hashemalayan.services.db.interfaces.CollectionService;
import me.hashemalayan.services.db.interfaces.IndexService;
import me.hashemalayan.services.db.interfaces.SchemaService;

public class BasicDatabaseService extends AbstractDatabaseService {

    @Inject
    public BasicDatabaseService(
            CollectionService collectionService,
            SchemaService schemaService,
            IndexService indexService
    ) {
        super(collectionService, schemaService, indexService);
    }
}
