package me.hashemalayan;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.nosql.shared.SignalingServiceGrpc;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SignalingClient {

    private final ManagedChannel channel;
    private final SignalingServiceGrpc.SignalingServiceStub stub;

    StreamObserver<PortContainingMessage> portContainingMessageStreamObserver;

    @Inject
    public SignalingClient(@Named("host") String host, @Named("port") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        stub = SignalingServiceGrpc.newStub(channel);
    }

    public void connect() {

        if (portContainingMessageStreamObserver == null) {
            this.portContainingMessageStreamObserver = stub.nodeStream(
                    new SignalingServerObserver()
            );
        }
    }

    public void sendMessage(Object message) {

    }

    public void announcePresence(String portToAnnouncePresenceOn) {

        Objects.requireNonNull(portContainingMessageStreamObserver).onNext(
                PortContainingMessage.newBuilder()
                        .setPort(portToAnnouncePresenceOn)
                        .build()
        );
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
