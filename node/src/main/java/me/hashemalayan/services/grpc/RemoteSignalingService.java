package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.factories.SignalingStreamMeshObserverFactory;
import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.nosql.shared.SignalingServiceGrpc;

import java.io.IOException;


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

    public void init() throws IOException {

        assert stub == null;

        ManagedChannel channel;

        if (nodeProperties.isUseSsl()) {
            final var credentials = TlsChannelCredentials.newBuilder()
                    .trustManager(nodeProperties.getCertificateAuthorityPath().toFile())
                    .build();

            channel = Grpc.newChannelBuilder(
                            "localhost:" + nodeProperties.getSignalingPort(),
                            credentials
                    )
                    .build();
        } else {
            channel = ManagedChannelBuilder
                    .forAddress("127.0.0.1", nodeProperties.getSignalingPort())
                    .usePlaintext()
                    .build();
        }
        stub = SignalingServiceGrpc.newStub(channel);

        stub.joinMeshStream(
                PortContainingMessage.newBuilder()
                        .setPort(nodeProperties.getPort())
                        .build(),
                signalingStreamMeshObserverFactory.create()
        );
    }
}
