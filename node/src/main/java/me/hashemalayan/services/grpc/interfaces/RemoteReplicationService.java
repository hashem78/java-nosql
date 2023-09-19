package me.hashemalayan.services.grpc.interfaces;

import me.hashemalayan.nosql.shared.Common;
import me.hashemalayan.nosql.shared.ReplicationMessage;

import java.io.IOException;

public interface RemoteReplicationService {
    void addReplica(int port) throws IOException;

    Common.CollectionDocument redirect(int nodeToRedirectTo, Common.SetCollectionDocumentRequest request);

    void broadcast(ReplicationMessage message);
}
