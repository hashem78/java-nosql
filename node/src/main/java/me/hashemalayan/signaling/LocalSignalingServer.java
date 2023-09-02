package me.hashemalayan.signaling;

import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.LoadDiscoveryResponse;
import me.hashemalayan.nosql.shared.NodeSignalingServiceGrpc;
import me.hashemalayan.nosql.shared.SignalingMessage;

public class LocalSignalingServer extends NodeSignalingServiceGrpc.NodeSignalingServiceImplBase {
    @Override
    public void uniDirectional(SignalingMessage request, StreamObserver<SignalingMessage> responseObserver) {
        responseObserver.onNext(
                SignalingMessage.newBuilder()
                        .setLoadDiscoveryResponse(
                                LoadDiscoveryResponse.newBuilder()
                                .setLoad(100)
                                .build()
                        )
                        .build()
        );
        responseObserver.onCompleted();
    }
}
