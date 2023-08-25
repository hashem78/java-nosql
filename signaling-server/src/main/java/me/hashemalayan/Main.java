package me.hashemalayan;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import me.hashemalayan.nosql.shared.PortContainingMessage;
import me.hashemalayan.nosql.shared.SignalingServiceGrpc;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class SignalingServerImpl extends SignalingServiceGrpc.SignalingServiceImplBase {

    final BiMap<String, StreamObserver<PortContainingMessage>> clientMap;

    SignalingServerImpl() {
        clientMap = HashBiMap.create();
    }

    @Override
    public StreamObserver<PortContainingMessage> nodeStream(StreamObserver<PortContainingMessage> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(PortContainingMessage request) {

                System.out.println("Received " + request);

                final Set<StreamObserver<PortContainingMessage>> clientsToCleanup = new HashSet<>();

                synchronized (clientMap) {
                    for (var client : clientMap.values()) {
                        try {
                            client.onNext(
                                    PortContainingMessage.newBuilder()
                                            .setPort(request.getPort())
                                            .build()
                            );
                        } catch (Exception e) {
                            clientsToCleanup.add(client);
                        }
                    }

                    for (var client : clientsToCleanup) {
                        cleanupClient(client);
                    }

                    clientMap.put(request.getPort(), responseObserver);
                }
            }

            @Override
            public void onError(Throwable t) {
                cleanupClient(responseObserver);
                System.out.println("An error happened in the stream: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                cleanupClient(responseObserver);
            }

            private void cleanupClient(StreamObserver<PortContainingMessage> observer) {

                synchronized (clientMap) {
                    clientMap.inverse().remove(observer);
                }

            }
        };
    }
}

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        var server = ServerBuilder.forPort(8080)
                .addService(new SignalingServerImpl())
                .build();

        server.start();
        server.awaitTermination();
    }
}