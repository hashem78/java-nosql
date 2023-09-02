package me.hashemalayan.signaling.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.signaling.RemoteSignalingClient;
import me.hashemalayan.signaling.events.InitializeSignalingClientEvent;

public class InitializeSignalingClientEventHandler implements EventHandler<InitializeSignalingClientEvent> {
    @Inject
    private RemoteSignalingClient remoteSignalingClient;
    @Override
    public void handle(InitializeSignalingClientEvent event) {

        remoteSignalingClient.connect();
    }

    @Override
    public Class<InitializeSignalingClientEvent> getHandledEventType() {
        return InitializeSignalingClientEvent.class;
    }
}
