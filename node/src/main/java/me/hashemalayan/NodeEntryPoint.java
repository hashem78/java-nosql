package me.hashemalayan;

import com.google.inject.Inject;
import me.hashemalayan.server.events.InitializeLocalServerEvent;
import me.hashemalayan.signaling.events.DiscoverRemoteNodesEvent;
import me.hashemalayan.signaling.events.InitializeSignalingClientEvent;
import me.hashemalayan.signaling.events.SendSignalingPrescenceEvent;

public class NodeEntryPoint {
    @Inject
    private EventLoop eventLoop;

    void run() {
        eventLoop.dispatch(new InitializeLocalServerEvent());
        eventLoop.dispatch(new InitializeSignalingClientEvent());
        eventLoop.dispatch(new SendSignalingPrescenceEvent());
        eventLoop.dispatch(new DiscoverRemoteNodesEvent());
        eventLoop.process();
    }
}
