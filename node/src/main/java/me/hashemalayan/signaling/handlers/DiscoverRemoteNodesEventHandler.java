package me.hashemalayan.signaling.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.SignalingClient;
import me.hashemalayan.server.RemoteNodesManager;
import me.hashemalayan.signaling.events.DiscoverRemoteNodesEvent;

public class DiscoverRemoteNodesEventHandler implements EventHandler<DiscoverRemoteNodesEvent> {
    final private SignalingClient signalingClient;
    final private RemoteNodesManager remoteNodesManager;

    @Inject
    public DiscoverRemoteNodesEventHandler(
            SignalingClient signalingClient,
            RemoteNodesManager remoteNodesManager
    ) {
        this.signalingClient = signalingClient;
        this.remoteNodesManager = remoteNodesManager;
    }

    @Override
    public void handle(DiscoverRemoteNodesEvent event) {
        System.out.println("Discovering Remote Nodes");
        var remoteNodes = signalingClient.discoverRemoteNodes(event.localPort());
        System.out.println("Found: " + remoteNodes);
        for (var remoteNode : remoteNodes) {
            remoteNodesManager.addRemoteNode(remoteNode);
            System.out.println("State of: " + remoteNodesManager.getNodeState(remoteNode));
        }
    }

    @Override
    public Class<DiscoverRemoteNodesEvent> getHandledEventType() {
        return DiscoverRemoteNodesEvent.class;
    }
}
