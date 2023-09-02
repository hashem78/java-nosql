package me.hashemalayan.signaling.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.signaling.RemoteSignalingClient;
import me.hashemalayan.signaling.events.SendSignalingPrescenceEvent;

public class SendSignalingMessageHandler implements EventHandler<SendSignalingPrescenceEvent> {
    private final RemoteSignalingClient remoteSignalingClient;

    @Inject
    public SendSignalingMessageHandler(RemoteSignalingClient remoteSignalingClient) {
        this.remoteSignalingClient = remoteSignalingClient;
    }

    @Override
    public void handle(SendSignalingPrescenceEvent event) {
        remoteSignalingClient.announcePresence();
    }

    @Override
    public Class<SendSignalingPrescenceEvent> getHandledEventType() {
        return SendSignalingPrescenceEvent.class;
    }
}
