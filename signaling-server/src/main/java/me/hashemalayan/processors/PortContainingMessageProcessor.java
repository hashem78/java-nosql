package me.hashemalayan.processors;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.managers.SignalingClientManager;
import me.hashemalayan.nosql.shared.SignalingMessage;

import java.util.Optional;

public class PortContainingMessageProcessor implements SignalingMessageProcessor {
    @Inject
    private SignalingClientManager signalingClientManager;

    @Override
    public Optional<SignalingMessage> process(
            SignalingMessage request,
            StreamObserver<SignalingMessage> responseObserver) {
        var actualRequest = request.getPortContainingMessage();
        signalingClientManager.addClient(actualRequest.getPort(), responseObserver);
        return Optional.empty();
    }
}
