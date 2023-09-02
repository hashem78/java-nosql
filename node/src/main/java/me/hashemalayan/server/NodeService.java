package me.hashemalayan.server;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.GetNodeStateRequest;
import me.hashemalayan.nosql.shared.GetNodeStateResponse;
import me.hashemalayan.nosql.shared.NodeServiceGrpc;
import me.hashemalayan.nosql.shared.NodeState;

public class NodeService extends NodeServiceGrpc.NodeServiceImplBase {
    @Inject
    private NodeProperties nodeProperties;

    @Override
    public void getNodeState(
            GetNodeStateRequest request,
            StreamObserver<GetNodeStateResponse> responseObserver) {

        System.out.println(request.getSender() + " asked for my state!");

        responseObserver.onNext(
                GetNodeStateResponse.newBuilder()
                        .setPort(nodeProperties.getPort())
                        .setNodeState(NodeState.HEAlTHY)
                        .build()
        );
        responseObserver.onCompleted();
    }
}
