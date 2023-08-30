package me.hashemalayan.signaling.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.server.RemoteNodesManager;
import me.hashemalayan.signaling.SignalingClient;
import me.hashemalayan.signaling.events.DiscoverRemoteNodesEvent;
import org.slf4j.Logger;

public class DiscoverRemoteNodesEventHandler implements EventHandler<DiscoverRemoteNodesEvent> {
    final private SignalingClient signalingClient;
    final private RemoteNodesManager remoteNodesManager;
    final private Logger logger;

    @Inject
    public DiscoverRemoteNodesEventHandler(
            SignalingClient signalingClient,
            RemoteNodesManager remoteNodesManager,
            Logger logger) {
        this.signalingClient = signalingClient;
        this.remoteNodesManager = remoteNodesManager;
        this.logger = logger;
    }

    @Override
    public void handle(DiscoverRemoteNodesEvent event) {
        logger.debug("Discovering Remote Nodes...");
        var remoteNodes = signalingClient.discoverRemoteNodes();
        logger.debug("Found: " + remoteNodes);
        for (var remoteNode : remoteNodes) {
            logger.debug("Adding RemoteNode: " + remoteNode);
            remoteNodesManager.addRemoteNode(remoteNode);
            logger.debug("Added Remote: " + remoteNode);
            logger.debug("State of: " + remoteNode + " is " + remoteNodesManager.getNodeState(remoteNode));
        }
    }

    @Override
    public Class<DiscoverRemoteNodesEvent> getHandledEventType() {
        return DiscoverRemoteNodesEvent.class;
    }
}
