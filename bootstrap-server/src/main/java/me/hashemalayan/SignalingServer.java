package me.hashemalayan;

import io.grpc.Grpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.*;

import java.io.IOException;
import java.util.*;

public class SignalingServer extends SignalingServiceGrpc.SignalingServiceImplBase {

    final private Set<Integer> connectedNodePorts;
    final private Map<Integer, StreamObserver<PortContainingMessage>> nodePortStreams;
    final private Map<Integer, LoadBalancingServiceGrpc.LoadBalancingServiceBlockingStub> loadBalancingServiceStubMap;

    private final BootstrapProperties properties;

    SignalingServer(BootstrapProperties properties) {
        this.properties = properties;
        this.nodePortStreams = Collections.synchronizedMap(new HashMap<>());
        this.connectedNodePorts = Collections.synchronizedSet(new HashSet<>());
        this.loadBalancingServiceStubMap = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void joinMeshStream(
            PortContainingMessage request,
            StreamObserver<PortContainingMessage> responseObserver
    ) {
        System.out.println(request.getPort() + " joined");
        // Broadcast to all previously connected nodes to let them know a new node just joined the mesh.
        var nodePortsStreamIterator = nodePortStreams.entrySet().iterator();
        while (nodePortsStreamIterator.hasNext()) {
            var entry = nodePortsStreamIterator.next();
            try {
                entry.getValue().onNext(request);
            } catch (Exception e) {

                System.out.println("Failed to send port containing message to " + entry.getKey());
                var disconnectedPort = entry.getKey();
                connectedNodePorts.remove(disconnectedPort);
                loadBalancingServiceStubMap.remove(disconnectedPort);
                nodePortsStreamIterator.remove();
            }
        }

        // Stream the currently connected nodes back to newly joined node
        for (final var port : nodePortStreams.keySet()) {
            responseObserver.onNext(
                    PortContainingMessage.newBuilder()
                            .setPort(port)
                            .build()
            );
        }
        connectedNodePorts.add(request.getPort());
        nodePortStreams.put(request.getPort(), responseObserver);

        try {

            LoadBalancingServiceGrpc.LoadBalancingServiceBlockingStub stub;

            if (properties.isUseSSL()) {
                System.out.println("Using SSL");
                final var credentials = TlsChannelCredentials.newBuilder()
                        .trustManager(properties.getCertificateAuthorityPath().toFile())
                        .build();
                stub = LoadBalancingServiceGrpc.newBlockingStub(
                        Grpc.newChannelBuilder(
                                "localhost:" + request.getPort(),
                                credentials
                        ).build()
                );
            } else {
                System.out.println("Not Using SSL");
                stub = LoadBalancingServiceGrpc.newBlockingStub(
                        ManagedChannelBuilder
                                .forAddress("127.0.0.1", request.getPort())
                                .usePlaintext()
                                .build());
            }

            loadBalancingServiceStubMap.put(request.getPort(), stub);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void discoverLoad(
            LoadDiscoveryRequest request,
            StreamObserver<LoadDiscoveryResponse> responseObserver
    ) {

        var connectedNodePortIterator = connectedNodePorts.iterator();
        var loadResponses = new ArrayList<LoadDiscoveryResponse>();
        while (connectedNodePortIterator.hasNext()) {
            var port = connectedNodePortIterator.next();
            try {
                var stub = loadBalancingServiceStubMap.get(port);
                var loadDiscoveryResponse = stub.discoverLoad(request);
                System.out.println("Responding with: " + loadDiscoveryResponse);
                loadResponses.add(loadDiscoveryResponse);
            } catch (Exception e) {
                System.err.println("Error discovering load for port " + port + ": " + e.getMessage());
                e.printStackTrace();
                connectedNodePortIterator.remove();
                nodePortStreams.remove(port);
                loadBalancingServiceStubMap.remove(port);
                responseObserver.onError(e);
                return;
            }
        }

        if(loadResponses.isEmpty()) {
            responseObserver.onCompleted();
        }

        // TODO: handle when there are no nodes in the mesh
        var nodeWithLeastLoad = loadResponses.stream()
                .min(Comparator.comparingInt(LoadDiscoveryResponse::getLoad));
        nodeWithLeastLoad.ifPresent(responseObserver::onNext);

        responseObserver.onCompleted();
    }

    @Override
    public void getAvailableNodes(
            GetAvailableNodesRequest request,
            StreamObserver<GetAvailableNodesResponse> responseObserver
    ) {
        responseObserver.onNext(
                GetAvailableNodesResponse.newBuilder()
                        .addAllNodePorts(connectedNodePorts)
                .build()
        );
        responseObserver.onCompleted();
    }
}
