package me.hashemalayan.managers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.SignalingMessage;

import java.util.ArrayList;
import java.util.List;


public class SignalingClientManager {
    final private BiMap<Integer, StreamObserver<SignalingMessage>> clientResponseMap;

    final private BiMap<Integer, RemoteNodeSignalingClient> clientStubs;

    public SignalingClientManager() {
        clientResponseMap = Maps.synchronizedBiMap(HashBiMap.create());
        clientStubs = Maps.synchronizedBiMap(HashBiMap.create());
    }

    public List<Integer> getAllPorts() {
        return new ArrayList<>(clientResponseMap.keySet());
    }

    public void addClient(int clientPort, StreamObserver<SignalingMessage> clientResponseObserver) {
        clientResponseMap.put(clientPort, clientResponseObserver);
        clientStubs.put(clientPort, new RemoteNodeSignalingClient(clientPort));
        System.out.println("Added " + clientPort + " with clientResponseObserver being: " + clientResponseObserver);
    }

    public List<SignalingMessage> sendToAll(SignalingMessage message) {
        final var responses = new ArrayList<SignalingMessage>();
        for (final var nodeSignalingClient : clientStubs.values()) {
            responses.add(nodeSignalingClient.send(message));
        }
        return responses;
    }

    public void sendResponseTo(SignalingMessage response, int clientPort) {
        System.out.println("Sending: " + response + ": to " + clientPort);

        clientResponseMap.get(clientPort).onNext(response);
    }
}
