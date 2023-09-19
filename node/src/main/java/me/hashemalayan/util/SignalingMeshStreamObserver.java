package me.hashemalayan.util;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.services.grpc.interfaces.RemoteReplicationService;

import java.io.IOException;

public class SignalingMeshStreamObserver implements StreamObserver<PortContainingMessage> {
    private final RemoteReplicationService remoteReplicationService;

    @Inject
    public SignalingMeshStreamObserver(
            RemoteReplicationService remoteReplicationService) {
        this.remoteReplicationService = remoteReplicationService;
    }

    @Override
    public void onNext(PortContainingMessage message) {
        try {
            remoteReplicationService.addReplica(message.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {

    }
}
