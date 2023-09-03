package me.hashemalayan.services;

import com.google.inject.Inject;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import me.hashemalayan.NodeProperties;

import java.io.IOException;

public class LocalNodeService {

    private Server server;

    private final NodeProperties nodeProperties;
    private final LoadBalancingService loadBalancingService;
    private final NodeService nodeService;

    @Inject
    public LocalNodeService(
            NodeProperties nodeProperties,
            LoadBalancingService loadBalancingService,
            NodeService nodeService
    ) {
        this.nodeProperties = nodeProperties;
        this.loadBalancingService = loadBalancingService;
        this.nodeService = nodeService;
    }

    public void init() throws IOException {

        assert server == null;

        server = ServerBuilder.forPort(nodeProperties.getPort())
                .addService(nodeService)
                .addService(loadBalancingService)
                .build();

        server.start();
    }

    public void awaitTermination() throws InterruptedException {
        server.awaitTermination();
    }

    public void cleanup() {

        if (server != null && !server.isShutdown()) {
            server.shutdown();
        }
    }
}
