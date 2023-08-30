package me.hashemalayan.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import me.hashemalayan.nosql.shared.GetNodeStateRequest;
import me.hashemalayan.nosql.shared.NodeServiceGrpc;
import me.hashemalayan.nosql.shared.NodeState;

public class NodeClient {

    private final NodeServiceGrpc.NodeServiceBlockingStub blockingStub;

    private final NodeServiceGrpc.NodeServiceStub asyncStub;

    private final Integer port;

    public NodeClient(Integer port) {
        this.port = port;
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();

        blockingStub = NodeServiceGrpc.newBlockingStub(channel);
        asyncStub = NodeServiceGrpc.newStub(channel);
    }

    public NodeState getNodeState() {
        var response = blockingStub.getNodeState(
                GetNodeStateRequest
                        .newBuilder()
                        .setSender(port)
                        .build()
        );
        return response.getNodeState();
    }
}
