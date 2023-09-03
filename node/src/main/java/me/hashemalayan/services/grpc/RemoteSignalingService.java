package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import io.grpc.ManagedChannelBuilder;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.factories.SignalingStreamMeshObserverFactory;
import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.nosql.shared.SignalingServiceGrpc;


public class RemoteSignalingService {

    private final NodeProperties nodeProperties;
    private final SignalingStreamMeshObserverFactory signalingStreamMeshObserverFactory;
    private SignalingServiceGrpc.SignalingServiceStub stub;

    @Inject
    public RemoteSignalingService(
            NodeProperties nodeProperties,
            SignalingStreamMeshObserverFactory signalingStreamMeshObserverFactory
    ) {
        this.nodeProperties = nodeProperties;
        this.signalingStreamMeshObserverFactory = signalingStreamMeshObserverFactory;
        stub = null;
    }

    public void init() {
        if (stub == null) {
            final var channel = ManagedChannelBuilder
                    .forAddress("127.0.0.1", nodeProperties.getSignalingPort())
                    .usePlaintext()
                    .build();
            stub = SignalingServiceGrpc.newStub(channel);
            stub.joinMeshStream(
                    PortContainingMessage.newBuilder()
                            .setPort(nodeProperties.getPort())
                            .build(),
                    signalingStreamMeshObserverFactory.create()
            );
        }
    }
}
