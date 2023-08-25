package me.hashemalayan.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import me.hashemalayan.nosql.shared.GetNodeStateRequest;
import me.hashemalayan.nosql.shared.NodeServiceGrpc;
import me.hashemalayan.nosql.shared.NodeState;

public class NodeClient {

    private final NodeServiceGrpc.NodeServiceBlockingStub blockingStub;

    private final NodeServiceGrpc.NodeServiceStub asyncStub;

    public NodeClient(String port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", Integer.parseInt(port))
                .usePlaintext()
                .build();

        blockingStub = NodeServiceGrpc.newBlockingStub(channel);
        asyncStub = NodeServiceGrpc.newStub(channel);
    }

    public NodeState getNodeState() {
        var response = blockingStub.getNodeState(GetNodeStateRequest.newBuilder().build());
        return response.getNodeState();
    }
}
