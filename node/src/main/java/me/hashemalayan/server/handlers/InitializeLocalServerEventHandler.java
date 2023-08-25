package me.hashemalayan.server.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.server.LocalNodeManager;
import me.hashemalayan.server.events.InitializeLocalServerEvent;

import java.io.IOException;

public class InitializeLocalServerEventHandler implements EventHandler<InitializeLocalServerEvent> {

    private final LocalNodeManager manager;

    @Inject
    public InitializeLocalServerEventHandler(LocalNodeManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(InitializeLocalServerEvent event) {
        try {
            System.out.println("Initializing NodeServer");
            manager.init(event.port());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<InitializeLocalServerEvent> getHandledEventType() {
        return InitializeLocalServerEvent.class;
    }
}
