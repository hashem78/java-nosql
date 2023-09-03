package me.hashemalayan.util;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.services.grpc.RemoteNodesService;
import org.slf4j.Logger;

public class SignalingMeshStreamObserver implements StreamObserver<PortContainingMessage> {

    private final RemoteNodesService remoteNodesService;
    private final Logger logger;

    @Inject
    public SignalingMeshStreamObserver(
            RemoteNodesService remoteNodesService,
            Logger logger
    ) {
        this.remoteNodesService = remoteNodesService;
        this.logger = logger;
    }

    @Override
    public void onNext(PortContainingMessage message) {
        logger.debug(message.getPort() + " is available");
        remoteNodesService.addRemoteNode(message.getPort());
        logger.debug("Its state is:" + remoteNodesService.getNodeState(message.getPort()));
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {

    }
}
