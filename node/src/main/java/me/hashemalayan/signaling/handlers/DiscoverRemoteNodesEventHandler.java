package me.hashemalayan.signaling.handlers;

import com.google.inject.Inject;
import me.hashemalayan.EventHandler;
import me.hashemalayan.server.RemoteNodesManager;
import me.hashemalayan.signaling.RemoteSignalingClient;
import me.hashemalayan.signaling.events.DiscoverRemoteNodesEvent;
import org.slf4j.Logger;

public class DiscoverRemoteNodesEventHandler implements EventHandler<DiscoverRemoteNodesEvent> {
    final private RemoteSignalingClient remoteSignalingClient;
    final private RemoteNodesManager remoteNodesManager;
    final private Logger logger;

    @Inject
    public DiscoverRemoteNodesEventHandler(
            RemoteSignalingClient remoteSignalingClient,
            RemoteNodesManager remoteNodesManager,
            Logger logger) {
        this.remoteSignalingClient = remoteSignalingClient;
        this.remoteNodesManager = remoteNodesManager;
        this.logger = logger;
    }

    @Override
    public void handle(DiscoverRemoteNodesEvent event) {
        logger.debug("Discovering Remote Nodes...");
        var remoteNodes = remoteSignalingClient.discoverRemoteNodes();
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
