package me.hashemalayan.managers;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.SignalingMessage;
import me.hashemalayan.processors.LoadDiscoveryRequestProcessor;
import me.hashemalayan.processors.PortContainingMessageProcessor;
import me.hashemalayan.processors.SignalingMessageProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static me.hashemalayan.nosql.shared.SignalingMessage.MessageCase.*;


public class SignalingProcessorsManager {
    final Map<SignalingMessage.MessageCase, SignalingMessageProcessor> processorMap;
    final private SignalingClientManager signalingClientManager;
    @Inject
    public SignalingProcessorsManager(
            SignalingClientManager signalingClientManager,
            PortContainingMessageProcessor portContainingMessageProcessor,
            LoadDiscoveryRequestProcessor loadDiscoveryRequestProcessor) {
        this.signalingClientManager = signalingClientManager;
        processorMap = new HashMap<>() {
            {
                put(PORTCONTAININGMESSAGE, portContainingMessageProcessor);
                put(LOADDISCOVERYRESPONSE, loadDiscoveryRequestProcessor);
            }
        };
    }

    public Optional<SignalingMessage> process(SignalingMessage request, StreamObserver<SignalingMessage> responseObserver) {

        System.out.println("Processing: " + request);
        return processorMap.get(request.getMessageCase()).process(request, responseObserver);
    }
}
