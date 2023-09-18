package me.hashemalayan.services.grpc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.nosql.shared.Common.SetCollectionDocumentRequest;
import me.hashemalayan.nosql.shared.ReplicationServiceGrpc.ReplicationServiceFutureStub;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class RemoteReplicationService {
    private final NodeProperties nodeProperties;
    private final Logger logger;
    private final BiMap<Integer, ReplicationServiceFutureStub> stubMap;

    @Inject
    public RemoteReplicationService(
            NodeProperties nodeProperties,
            Logger logger
    ) {
        this.nodeProperties = nodeProperties;
        this.logger = logger;
        stubMap = Maps.synchronizedBiMap(HashBiMap.create());
    }

    public void addReplica(int port) throws IOException {


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

        stubMap.put(port, ReplicationServiceGrpc.newFutureStub(channel));
        logger.info("Added replica " + port);
    }

    public CollectionDocument redirect(int nodeToRedirectTo, SetCollectionDocumentRequest request) {
        try {
            logger.warn("Redirecting SetCollectionDocumentRequest to " + nodeToRedirectTo);
            return stubMap.get(nodeToRedirectTo).collectionDocumentRedirection(request).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcast(ReplicationMessage message) {
        logger.info("Broadcasting " + message);
        switch (message.getMessageCase()) {
            case CREATE_COLLECTION_REPLICATION_MESSAGE ->
                    internalBroadcast(x -> x.createCollection(message.getCreateCollectionReplicationMessage()));
            case EDIT_COLLECTION_REPLICATION_MESSAGE ->
                    internalBroadcast(x -> x.editCollection(message.getEditCollectionReplicationMessage()));
            case DELETE_COLLECTION_REPLICATION_MESSAGE ->
                    internalBroadcast(x -> x.deleteCollection(message.getDeleteCollectionReplicationMessage()));
            case SET_DOCUMENT_REPLICATION_MESSAGE ->
                    internalBroadcast(x -> x.setCollectionDocument(message.getSetDocumentReplicationMessage()));
            case DELETE_DOCUMENT_REPLICATION_MESSAGE ->
                    internalBroadcast(x -> x.deleteCollectionDocument(message.getDeleteDocumentReplicationMessage()));
            case INDEX_COLLECTION_PROPERTY_REPLICATION_MESSAGE ->
                    internalBroadcast(x -> x.indexCollectionProperty(message.getIndexCollectionPropertyReplicationMessage()));
            case REMOVE_INDEX_REPLICATION_MESSAGE ->
                internalBroadcast(x -> x.removeIndexFromCollectionProperty(message.getRemoveIndexReplicationMessage()));
            case MESSAGE_NOT_SET -> {
            }
        }
    }

    private void internalBroadcast(
            Function<ReplicationServiceFutureStub, ListenableFuture<ReplicationResponse>> messenger
    ) {

        final var responseFutures = new ArrayList<ListenableFuture<ReplicationResponse>>();
        final var replicaPorts = new ArrayList<Integer>();

        for (final var entry : stubMap.entrySet()) {
            logger.debug("- to " + entry.getKey());
            final var response = messenger.apply(entry.getValue());
            responseFutures.add(response);
            replicaPorts.add(entry.getKey());
        }

        try {
            final var responses = Futures.successfulAsList(responseFutures).get();
            int index = 0;
            for (final var response : responses) {
                final var portOfNode = replicaPorts.get(index);
                if (response == null) {
                    logger.error("- " + portOfNode + " failed to respond");
                } else {
                    logger.debug("- " + portOfNode + " responded successfully");
                }
                index++;
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Broadcast interrupted");
            e.printStackTrace();
        }
    }
}
