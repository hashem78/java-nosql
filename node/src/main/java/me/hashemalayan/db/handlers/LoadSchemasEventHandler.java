package me.hashemalayan.db.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.db.SchemaManager;
import me.hashemalayan.db.events.LoadSchemasEvent;

public class LoadSchemasEventHandler implements EventHandler<LoadSchemasEvent> {

    @Inject
    private SchemaManager schemaManager;

    @Override
    public void handle(LoadSchemasEvent event) {
        schemaManager.load();
    }

    @Override
    public Class<LoadSchemasEvent> getHandledEventType() {
        return LoadSchemasEvent.class;
    }
}
