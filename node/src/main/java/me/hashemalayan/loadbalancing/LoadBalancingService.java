package me.hashemalayan.loadbalancing;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.LoadBalancingServiceGrpc;
import me.hashemalayan.nosql.shared.LoadDiscoveryRequest;
import me.hashemalayan.nosql.shared.LoadDiscoveryResponse;

public class LoadBalancingService extends LoadBalancingServiceGrpc.LoadBalancingServiceImplBase {

    @Inject
    private NodeProperties nodeProperties;

    @Override
    public void discoverLoad(
            LoadDiscoveryRequest request,
            StreamObserver<LoadDiscoveryResponse> responseObserver
    ) {
        responseObserver.onNext(
                LoadDiscoveryResponse.newBuilder()
                        .setPort(nodeProperties.getPort())
                        .setLoad(nodeProperties.getLoad())
                .build()
        );

        responseObserver.onCompleted();
    }
}
