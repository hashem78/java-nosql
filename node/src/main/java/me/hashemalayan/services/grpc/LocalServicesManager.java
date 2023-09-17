package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.util.interceptors.ExceptionHandlingInterceptor;
import me.hashemalayan.util.interceptors.LoggingInterceptor;
import org.slf4j.Logger;

import java.io.IOException;

public class LocalServicesManager {

    private Server server;
    private final NodeProperties nodeProperties;
    private final LoadBalancingService loadBalancingService;
    private final LocalNodeService localNodeService;
    final private ExceptionHandlingInterceptor exceptionHandlingInterceptor;
    final private LoggingInterceptor loggingInterceptor;
    private final LocalReplicationService localReplicationService;

    private final Logger logger;

    @Inject
    public LocalServicesManager(
            NodeProperties nodeProperties,
            LoadBalancingService loadBalancingService,
            LocalNodeService localNodeService,
            ExceptionHandlingInterceptor exceptionHandlingInterceptor,
            LoggingInterceptor loggingInterceptor,
            LocalReplicationService localReplicationService,
            Logger logger) {
        this.nodeProperties = nodeProperties;
        this.loadBalancingService = loadBalancingService;
        this.localNodeService = localNodeService;
        this.exceptionHandlingInterceptor = exceptionHandlingInterceptor;
        this.loggingInterceptor = loggingInterceptor;
        this.localReplicationService = localReplicationService;
        this.logger = logger;
    }

    public void init() throws IOException {

        assert server == null;

        final var serverBuilder = ServerBuilder.forPort(nodeProperties.getPort());

        if (nodeProperties.isUseSsl()) {
            logger.info("Using SSL");
            serverBuilder.useTransportSecurity(
                    nodeProperties.getSslCertificatePath().toFile(),
                    nodeProperties.getPrivateKeyPath().toFile()
            );
        }

        server = serverBuilder
                .addService(localNodeService)
                .addService(loadBalancingService)
                .addService(localReplicationService)
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
