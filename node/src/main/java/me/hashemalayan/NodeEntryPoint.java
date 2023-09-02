package me.hashemalayan;

import com.google.inject.Inject;
import me.hashemalayan.db.events.InitializeLocalDatabaseEvent;
import me.hashemalayan.db.events.LoadSchemasEvent;
import me.hashemalayan.db.events.ValidateAllSchemasEvent;
import me.hashemalayan.server.events.InitializeLocalServerEvent;
import me.hashemalayan.signaling.events.InitializeSignalingClientEvent;

public class NodeEntryPoint {
    @Inject
    private EventLoop eventLoop;

    void run() {
        eventLoop.dispatch(new InitializeLocalServerEvent());
        eventLoop.dispatch(new InitializeSignalingClientEvent());
        eventLoop.dispatch(new InitializeLocalDatabaseEvent());
        eventLoop.dispatch(new LoadSchemasEvent());
        eventLoop.dispatch(new ValidateAllSchemasEvent());
        eventLoop.process();
    }
}
