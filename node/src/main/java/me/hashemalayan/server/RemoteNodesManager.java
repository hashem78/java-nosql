package me.hashemalayan.server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.inject.Inject;
import io.vavr.Tuple2;
import me.hashemalayan.client.NodeClient;
import me.hashemalayan.nosql.shared.NodeState;

import java.util.ArrayList;
import java.util.List;

public class RemoteNodesManager {

    final private BiMap<Integer, NodeClient> clients;

    @Inject
    public RemoteNodesManager() {
        clients = HashBiMap.create();
    }

    public void addRemoteNode(Integer port) {
        clients.put(port, new NodeClient(port));
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
}
