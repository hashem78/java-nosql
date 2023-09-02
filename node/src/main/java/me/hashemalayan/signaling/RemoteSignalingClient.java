package me.hashemalayan.signaling;

import com.google.inject.Inject;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.NodeDiscoveryRequest;
import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.nosql.shared.SignalingMessage;
import me.hashemalayan.nosql.shared.SignalingServiceGrpc;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class RemoteSignalingClient {

    private final ManagedChannel channel;
    private final SignalingServiceGrpc.SignalingServiceStub asyncStub;

    private final SignalingServiceGrpc.SignalingServiceBlockingStub blockingStub;

    private final NodeProperties nodeProperties;

    StreamObserver<SignalingMessage> nodeStreamObserver;

    @Inject
    public RemoteSignalingClient(NodeProperties nodeProperties) {

        this.nodeProperties = nodeProperties;
        this.channel = ManagedChannelBuilder.forAddress("127.0.0.1", nodeProperties.getSignalingPort())
                .usePlaintext()
                .build();

        asyncStub = SignalingServiceGrpc.newStub(channel);
        blockingStub = SignalingServiceGrpc.newBlockingStub(channel);
    }

    public void connect() {

        if (nodeStreamObserver == null) {
            this.nodeStreamObserver = Objects.requireNonNull(asyncStub).nodeStream(
                    new RemoteSignalingServerObserver()
            );
        }
    }

    public void announcePresence() {

        Objects.requireNonNull(nodeStreamObserver).onNext(
                SignalingMessage.newBuilder()
                        .setPortContainingMessage(
                                PortContainingMessage.newBuilder()
                                        .setPort(nodeProperties.getPort())
                                        .build()
                        )
                        .setSenderPort(nodeProperties.getPort())
                        .build()
        );
    }

    public List<Integer> discoverRemoteNodes() {
        var response = Objects.requireNonNull(blockingStub).nodeDiscovery(
                NodeDiscoveryRequest.newBuilder()
                        .setLocalPort(nodeProperties.getPort())
                        .build()
        );

        return response.getPortsList();
    }

    public void shutdown() throws InterruptedException {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static class RemoteSignalingServerObserver implements StreamObserver<SignalingMessage> {

        @Override
        public void onNext(SignalingMessage response) {

        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
        }

        @Override
        public void onCompleted() {
            System.out.println("Server completed sending messages");
        }
    }
}
