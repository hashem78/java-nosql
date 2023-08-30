package me.hashemalayan.signaling.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.signaling.SignalingClient;
import me.hashemalayan.signaling.events.InitializeSignalingClientEvent;

public class InitializeSignalingClientEventHandler implements EventHandler<InitializeSignalingClientEvent> {
    @Inject
    private SignalingClient signalingClient;
    @Override
    public void handle(InitializeSignalingClientEvent event) {

        signalingClient.connect();
    }

    @Override
    public Class<InitializeSignalingClientEvent> getHandledEventType() {
        return InitializeSignalingClientEvent.class;
    }
}
