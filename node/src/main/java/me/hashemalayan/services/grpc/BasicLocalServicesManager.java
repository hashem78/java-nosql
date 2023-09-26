package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.LoadBalancingServiceGrpc.LoadBalancingServiceImplBase;
import me.hashemalayan.nosql.shared.NodeServiceGrpc.NodeServiceImplBase;
import me.hashemalayan.nosql.shared.ReplicationServiceGrpc.ReplicationServiceImplBase;
import me.hashemalayan.services.grpc.interfaces.LocalAuthService;
import me.hashemalayan.services.grpc.interfaces.LocalServicesManager;
import me.hashemalayan.util.interceptors.ExceptionHandlingInterceptor;
import me.hashemalayan.util.interceptors.LoggingInterceptor;
import org.slf4j.Logger;

import java.io.IOException;

public class BasicLocalServicesManager implements LocalServicesManager {

    private Server server;
    private final NodeProperties nodeProperties;
    private final LoadBalancingServiceImplBase loadBalancingService;
    private final NodeServiceImplBase localNodeService;
    final private ExceptionHandlingInterceptor exceptionHandlingInterceptor;
    final private LoggingInterceptor loggingInterceptor;
    private final ReplicationServiceImplBase localReplicationService;
    private final LocalAuthService localAuthService;

    private final Logger logger;

    @Inject
    public BasicLocalServicesManager(
            NodeProperties nodeProperties,
            LoadBalancingServiceImplBase loadBalancingService,
            NodeServiceImplBase localNodeService,
            ExceptionHandlingInterceptor exceptionHandlingInterceptor,
            LoggingInterceptor loggingInterceptor,
            ReplicationServiceImplBase localReplicationService,
            LocalAuthService localAuthService,
            Logger logger) {
        this.nodeProperties = nodeProperties;
        this.loadBalancingService = loadBalancingService;
        this.localNodeService = localNodeService;
        this.exceptionHandlingInterceptor = exceptionHandlingInterceptor;
        this.loggingInterceptor = loggingInterceptor;
        this.localReplicationService = localReplicationService;
        this.localAuthService = localAuthService;
        this.logger = logger;
    }

    @Override
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
                .addService(localAuthService)
                .intercept(exceptionHandlingInterceptor)
                .intercept(loggingInterceptor)
                .build();

        server.start();
    }

    @Override
    public void awaitTermination() throws InterruptedException {
        server.awaitTermination();
    }

    @Override
    public void cleanup() {

        if (server != null && !server.isShutdown()) {
            server.shutdown();
        }
    }
}
