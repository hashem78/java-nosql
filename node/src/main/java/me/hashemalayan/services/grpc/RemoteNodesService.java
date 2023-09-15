package me.hashemalayan.services.grpc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.inject.Inject;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.GetNodeStateRequest;
import me.hashemalayan.nosql.shared.NodeServiceGrpc;
import me.hashemalayan.nosql.shared.NodeState;
import me.hashemalayan.util.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteNodesService {

    final private BiMap<Integer, RemoteNodeService> clients;

    final private NodeProperties nodeProperties;

    @Inject
    public RemoteNodesService(NodeProperties nodeProperties) {
        this.nodeProperties = nodeProperties;
        clients = HashBiMap.create();
    }

    public void addRemoteNode(Integer port) {
        try {
            clients.put(port, new RemoteNodeService(nodeProperties, port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NodeState getNodeState(Integer nodePort) {
        return clients.get(nodePort).getNodeState();
    }

    public List<Tuple2<Integer, NodeState>> getAllNodeStates() {
        var states = new ArrayList<Tuple2<Integer, NodeState>>();
        for (var entry : clients.entrySet()) {
            states.add(new Tuple2<>(entry.getKey(), entry.getValue().getNodeState()));
        }
        return states;
    }

    private static class RemoteNodeService {

        private final NodeServiceGrpc.NodeServiceBlockingStub blockingStub;

        private final Integer port;

        public RemoteNodeService(NodeProperties nodeProperties, Integer port) throws IOException {
            this.port = port;

            ManagedChannel channel;

            if (nodeProperties.isUseSsl()) {
                final var credentials = TlsChannelCredentials.newBuilder()
                        .trustManager(nodeProperties.getCertificateAuthorityPath().toFile())
                        .build();

                channel = Grpc.newChannelBuilder(
                                "localhost:" + port,
                                credentials
                        )
                        .build();

            } else {
                channel = ManagedChannelBuilder.forAddress("127.0.0.1", port)
                        .usePlaintext()
                        .build();
            }

            blockingStub = NodeServiceGrpc.newBlockingStub(channel);
        }

        public NodeState getNodeState() {
            var response = blockingStub.getNodeState(
                    GetNodeStateRequest
                            .newBuilder()
                            .setSender(port)
                            .build()
            );
            return response.getNodeState();
        }
    }

}
