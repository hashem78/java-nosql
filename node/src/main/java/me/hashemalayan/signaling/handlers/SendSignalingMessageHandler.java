package me.hashemalayan.signaling.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.SignalingClient;
import me.hashemalayan.signaling.events.SendSignalingPrescenceEvent;

public class SendSignalingMessageHandler implements EventHandler<SendSignalingPrescenceEvent> {
    @Inject
    private SignalingClient signalingClient;
    @Override
    public void handle(SendSignalingPrescenceEvent event) {
        signalingClient.announcePresence(event.port());
    }

    @Override
    public Class<SendSignalingPrescenceEvent> getHandledEventType() {
        return SendSignalingPrescenceEvent.class;
    }
}
