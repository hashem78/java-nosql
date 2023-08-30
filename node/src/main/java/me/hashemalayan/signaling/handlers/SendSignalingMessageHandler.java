package me.hashemalayan.signaling.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.signaling.SignalingClient;
import me.hashemalayan.signaling.events.SendSignalingPrescenceEvent;

public class SendSignalingMessageHandler implements EventHandler<SendSignalingPrescenceEvent> {
    private final SignalingClient signalingClient;

    @Inject
    public SendSignalingMessageHandler(SignalingClient signalingClient) {
        this.signalingClient = signalingClient;
    }

    @Override
    public void handle(SendSignalingPrescenceEvent event) {
        signalingClient.announcePresence();
    }

    @Override
    public Class<SendSignalingPrescenceEvent> getHandledEventType() {
        return SendSignalingPrescenceEvent.class;
    }
}
