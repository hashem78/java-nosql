package me.hashemalayan.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class LocalNodeManager {

    private Server server;

    public void init(String port) throws IOException {

        assert server == null;

        server = ServerBuilder.forPort(Integer.parseInt(port))
                .addService(new NodeServiceImpl(port))
                .build();

        server.start();
    }

    public void cleanup() {

        if (server != null && !server.isShutdown()) {
            server.shutdown();
        }
    }
}
