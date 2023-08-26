package me.hashemalayan.server;

import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.GetNodeStateRequest;
import me.hashemalayan.nosql.shared.GetNodeStateResponse;
import me.hashemalayan.nosql.shared.NodeServiceGrpc;
import me.hashemalayan.nosql.shared.NodeState;

public class NodeServiceImpl extends NodeServiceGrpc.NodeServiceImplBase {
    final String port;
    public NodeServiceImpl(String port) {
        this.port = port;
    }

    @Override
    public void getNodeState(
            GetNodeStateRequest request,
            StreamObserver<GetNodeStateResponse> responseObserver) {

        System.out.println(request.getSender() + " is asked for my state!");

        responseObserver.onNext(
                GetNodeStateResponse.newBuilder()
                        .setPort(port)
                        .setNodeState(NodeState.HEAlTHY)
                        .build()
        );
        responseObserver.onCompleted();
    }
}
