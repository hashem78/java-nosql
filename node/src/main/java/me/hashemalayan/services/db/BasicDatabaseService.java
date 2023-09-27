package me.hashemalayan.services.db;

import com.google.inject.Inject;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;
import me.hashemalayan.services.db.interfaces.CollectionService;
import me.hashemalayan.services.db.interfaces.IndexService;
import me.hashemalayan.services.db.interfaces.SchemaService;
import me.hashemalayan.util.CustomStructToJson;

public class BasicDatabaseService extends AbstractDatabaseService {

    @Inject
    public BasicDatabaseService(
            CollectionService collectionService,
            SchemaService schemaService,
            IndexService indexService,
            CustomStructToJson customStructToJson
    ) {
        super(collectionService, schemaService, indexService, customStructToJson);
    }
}
