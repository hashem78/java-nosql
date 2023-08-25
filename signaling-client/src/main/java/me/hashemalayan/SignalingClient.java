package me.hashemalayan;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.NodeDiscoveryRequest;
import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.nosql.shared.SignalingServiceGrpc;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SignalingClient {

    private final ManagedChannel channel;
    private final SignalingServiceGrpc.SignalingServiceStub asyncStub;

    private final SignalingServiceGrpc.SignalingServiceBlockingStub blockingStub;

    StreamObserver<PortContainingMessage> nodeStreamObserver;

    @Inject
    public SignalingClient(@Named("host") String host, @Named("port") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        asyncStub = SignalingServiceGrpc.newStub(channel);
        blockingStub = SignalingServiceGrpc.newBlockingStub(channel);
    }

    public void connect() {

        if (nodeStreamObserver == null) {
            this.nodeStreamObserver = Objects.requireNonNull(asyncStub).nodeStream(
                    new SignalingServerObserver()
            );
        }
    }

    public void announcePresence(String portToAnnouncePresenceOn) {

        Objects.requireNonNull(nodeStreamObserver).onNext(
                PortContainingMessage.newBuilder()
                        .setPort(portToAnnouncePresenceOn)
                        .build()
        );
    }

    public List<String> discoverRemoteNodes(String localPort) {
        var response = Objects.requireNonNull(blockingStub).nodeDiscovery(
                NodeDiscoveryRequest.newBuilder()
                        .setLocalPort(localPort)
                        .build()
        );

        return response.getPortsList();
    }

    public void shutdown() throws InterruptedException {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static class SignalingServerObserver implements StreamObserver<PortContainingMessage> {

        @Override
        public void onNext(PortContainingMessage response) {
            System.out.println(response.getPort() + " Wants to connect");
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
