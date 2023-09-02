package me.hashemalayan.processors;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.managers.SignalingClientManager;
import me.hashemalayan.nosql.shared.SignalingMessage;

import java.util.Comparator;
import java.util.Optional;

public class LoadDiscoveryRequestProcessor implements SignalingMessageProcessor {

    @Inject
    private SignalingClientManager signalingClientManager;

    @Override
    public Optional<SignalingMessage> process(SignalingMessage request, StreamObserver<SignalingMessage> responseObserver) {
        final var loadResponses = signalingClientManager.sendToAll(request);
        SignalingMessage responseWithLowestLoad;
        return loadResponses.stream().min(
                Comparator.comparingInt(x -> x.getLoadDiscoveryResponse().getLoad())
        );
    }
}
