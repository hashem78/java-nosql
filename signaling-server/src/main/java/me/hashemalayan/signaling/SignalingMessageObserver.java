package me.hashemalayan.signaling;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.managers.SignalingClientManager;
import me.hashemalayan.managers.SignalingProcessorsManager;
import me.hashemalayan.nosql.shared.SignalingMessage;

public class SignalingMessageObserver implements StreamObserver<SignalingMessage> {

    private final SignalingClientManager signalingClientManager;

    private final SignalingProcessorsManager signalingProcessorsManager;

    @Inject
    public SignalingMessageObserver(
            @Assisted SignalingClientManager signalingClientManager,
            @Assisted SignalingProcessorsManager signalingProcessorsManager) {
        this.signalingClientManager = signalingClientManager;
        this.signalingProcessorsManager = signalingProcessorsManager;
    }


    @Override
    public void onNext(SignalingMessage request) {

        System.out.println("Received: " + request);

        var response = signalingProcessorsManager.process(request, this);
        response.ifPresent(resp -> signalingClientManager.sendResponseTo(resp, request.getSenderPort()));
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {

    }
}
