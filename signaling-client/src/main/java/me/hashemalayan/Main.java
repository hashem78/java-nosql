package me.hashemalayan;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.PortContainingMessage;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static me.hashemalayan.nosql.shared.SignalingServiceGrpc.*;

class SignalingClient {

    private final ManagedChannel channel;
    private final CountDownLatch latch;

    public SignalingClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.latch = new CountDownLatch(1);
    }

    public void startStream() {
        var asyncStub = newStub(channel);

        var requestObserver = asyncStub.nodeStream(
                new SignalingServerObserver(latch)
        );

        requestObserver.onNext(
                PortContainingMessage.newBuilder()
                        .setPort("8084")
                        .build()
        );
    }

    public void awaitCompletion() throws InterruptedException {
        latch.await();
    }

    public void shutdown() throws InterruptedException {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static class SignalingServerObserver implements StreamObserver<PortContainingMessage> {

        final CountDownLatch latch;

        SignalingServerObserver(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onNext(PortContainingMessage response) {
            System.out.println(response.getPort() + " Wants to connect");
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
            latch.countDown();
        }

        @Override
        public void onCompleted() {
            System.out.println("Server completed sending messages");
            latch.countDown();
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {

        var client = new SignalingClient("localhost", 8080);
        client.startStream();
        client.awaitCompletion();
        client.shutdown();
    }
}