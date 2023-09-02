package me.hashemalayan.signaling;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.server.RemoteNodesManager;
import org.slf4j.Logger;

public class SignalingMeshStreamObserver implements StreamObserver<PortContainingMessage> {

    private final RemoteNodesManager remoteNodesManager;
    private final Logger logger;

    @Inject
    public SignalingMeshStreamObserver(
            RemoteNodesManager remoteNodesManager,
            Logger logger
    ) {
        this.remoteNodesManager = remoteNodesManager;
        this.logger = logger;
    }

    @Override
    public void onNext(PortContainingMessage message) {
        logger.debug(message.getPort() + " is available");
        remoteNodesManager.addRemoteNode(message.getPort());
        logger.debug("Its state is:" + remoteNodesManager.getNodeState(message.getPort()));
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {

    }
}
