package me.hashemalayan.signaling;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.managers.SignalingClientManager;
import me.hashemalayan.managers.SignalingProcessorsManager;
import me.hashemalayan.nosql.shared.*;

public class SignalingServerImpl extends SignalingServiceGrpc.SignalingServiceImplBase {

    @Inject
    private SignalingClientManager signalingClientManager;

    @Inject
    SignalingProcessorsManager signalingProcessorsManager;

    @Inject
    private SignalingMessageObserverFactory signalingMessageObserverFactory;

    @Override
    public StreamObserver<SignalingMessage> nodeStream(
            StreamObserver<SignalingMessage> responseObserver
    ) {
        return signalingMessageObserverFactory.create(signalingClientManager, signalingProcessorsManager);
    }

    @Override
    public void nodeDiscovery(NodeDiscoveryRequest request, StreamObserver<NodeDiscoveryResponse> responseObserver) {
        responseObserver.onNext(
                NodeDiscoveryResponse.newBuilder()
                        .addAllPorts(signalingClientManager.getAllPorts())
                        .build()
        );
        System.out.println(
                "Received Node Discovery Request from: "
                        + request.getLocalPort()
                        + ", returning: "
                        + signalingClientManager.getAllPorts()
        );
        responseObserver.onCompleted();
    }
}
