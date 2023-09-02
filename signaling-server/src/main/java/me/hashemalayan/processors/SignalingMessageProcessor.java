package me.hashemalayan.processors;

import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.SignalingMessage;

import java.util.Optional;

public interface SignalingMessageProcessor {
    Optional<SignalingMessage> process(SignalingMessage request, StreamObserver<SignalingMessage> responseObserver);
}
