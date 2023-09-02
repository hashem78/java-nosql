package me.hashemalayan.managers;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import me.hashemalayan.nosql.shared.NodeSignalingServiceGrpc;
import me.hashemalayan.nosql.shared.SignalingMessage;

public class RemoteNodeSignalingClient {
    final private NodeSignalingServiceGrpc.NodeSignalingServiceBlockingStub blockingStub;
    final private ManagedChannel channel;

    RemoteNodeSignalingClient(int clientPort) {
        channel = ManagedChannelBuilder.forAddress("127.0.0.1", clientPort).build();
        blockingStub = NodeSignalingServiceGrpc.newBlockingStub(channel);
    }

    SignalingMessage send(SignalingMessage message) {
        return blockingStub.uniDirectional(message);
    }
}
