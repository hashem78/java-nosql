package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.util.ExceptionHandlingInterceptor;

import java.io.IOException;

public class LocalServicesManager {

    private Server server;
    private final NodeProperties nodeProperties;
    private final LoadBalancingService loadBalancingService;
    private final LocalNodeService localNodeService;

    @Inject
    public LocalServicesManager(
            NodeProperties nodeProperties,
            LoadBalancingService loadBalancingService,
            LocalNodeService localNodeService
    ) {
        this.nodeProperties = nodeProperties;
        this.loadBalancingService = loadBalancingService;
        this.localNodeService = localNodeService;
    }

    public void init() throws IOException {

        assert server == null;

        server = ServerBuilder.forPort(nodeProperties.getPort())
                .addService(localNodeService)
                .addService(loadBalancingService)
                .intercept(new ExceptionHandlingInterceptor())
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
