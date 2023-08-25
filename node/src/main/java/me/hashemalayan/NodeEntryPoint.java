package me.hashemalayan;

import com.google.inject.Inject;
import me.hashemalayan.signaling.events.InitializeSignalingClientEvent;
import me.hashemalayan.signaling.events.SendSignalingPrescenceEvent;

public class NodeEntryPoint {
    @Inject
    private EventLoop eventLoop;

    void run(String grpcServerPort) {
        eventLoop.dispatch(new InitializeSignalingClientEvent());
        eventLoop.dispatch(new SendSignalingPrescenceEvent(grpcServerPort));
        eventLoop.process();
    }
}
