package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.util.interceptors.ExceptionHandlingInterceptor;
import me.hashemalayan.util.interceptors.LoggingInterceptor;

import java.io.IOException;

public class LocalServicesManager {

    private Server server;
    private final NodeProperties nodeProperties;
    private final LoadBalancingService loadBalancingService;
    private final LocalNodeService localNodeService;
    final private ExceptionHandlingInterceptor exceptionHandlingInterceptor;
    final private LoggingInterceptor loggingInterceptor;

    @Inject
    public LocalServicesManager(
            NodeProperties nodeProperties,
            LoadBalancingService loadBalancingService,
            LocalNodeService localNodeService,
            ExceptionHandlingInterceptor exceptionHandlingInterceptor,
            LoggingInterceptor loggingInterceptor
    ) {
        this.nodeProperties = nodeProperties;
        this.loadBalancingService = loadBalancingService;
        this.localNodeService = localNodeService;
        this.exceptionHandlingInterceptor = exceptionHandlingInterceptor;
        this.loggingInterceptor = loggingInterceptor;
    }

    public void init() throws IOException {

        assert server == null;

        server = ServerBuilder.forPort(nodeProperties.getPort())
                .addService(localNodeService)
                .addService(loadBalancingService)
                .intercept(exceptionHandlingInterceptor)
                .intercept(loggingInterceptor)
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
