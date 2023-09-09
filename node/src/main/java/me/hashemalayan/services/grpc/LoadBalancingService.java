package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.LoadBalancingServiceGrpc;
import me.hashemalayan.nosql.shared.LoadDiscoveryRequest;
import me.hashemalayan.nosql.shared.LoadDiscoveryResponse;
import me.hashemalayan.services.ClientCounterService;

public class LoadBalancingService extends LoadBalancingServiceGrpc.LoadBalancingServiceImplBase {

    private final ClientCounterService clientCounterService;
    private final NodeProperties nodeProperties;

    @Inject
    public LoadBalancingService(
            ClientCounterService clientCounterService,
            NodeProperties nodeProperties
    ) {
        this.clientCounterService = clientCounterService;
        this.nodeProperties = nodeProperties;
    }

    @Override
    public void discoverLoad(
            LoadDiscoveryRequest request,
            StreamObserver<LoadDiscoveryResponse> responseObserver
    ) {
        responseObserver.onNext(
                LoadDiscoveryResponse.newBuilder()
                        .setPort(nodeProperties.getPort())
                        .setLoad(clientCounterService.get())
                .build()
        );

        responseObserver.onCompleted();
    }
}
