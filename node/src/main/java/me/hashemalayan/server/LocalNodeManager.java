package me.hashemalayan.server;

import com.google.inject.Inject;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import me.hashemalayan.NodeProperties;

import java.io.IOException;

public class LocalNodeManager {

    private Server server;
    private NodeProperties nodeProperties;

    @Inject
    LocalNodeManager(NodeProperties nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

    public void init() throws IOException {

        assert server == null;

        server = ServerBuilder.forPort(nodeProperties.getPort())
                .addService(new NodeServiceImpl(nodeProperties.getPort()))
                .build();

        server.start();
    }

    public void cleanup() {

        if (server != null && !server.isShutdown()) {
            server.shutdown();
        }
    }
}
