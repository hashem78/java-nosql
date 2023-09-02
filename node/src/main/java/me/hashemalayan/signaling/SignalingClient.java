package me.hashemalayan.signaling;

import com.google.inject.Inject;
import io.grpc.ManagedChannelBuilder;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.nosql.shared.SignalingServiceGrpc;


public class SignalingClient {

    @Inject
    private NodeProperties nodeProperties;
    @Inject
    SignalingStreamMeshObserverFactory signalingStreamMeshObserverFactory;
    private SignalingServiceGrpc.SignalingServiceStub stub;

    public SignalingClient() {
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
