package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.services.db.DatabaseService;
import me.hashemalayan.services.db.exceptions.CollectionAlreadyExistsException;
import org.slf4j.Logger;

import java.io.IOException;

public class LocalNodeService extends NodeServiceGrpc.NodeServiceImplBase {

    private final DatabaseService databaseService;
    private final NodeProperties nodeProperties;

    private final Logger logger;
    @Inject
    public LocalNodeService(
            DatabaseService databaseService,
            NodeProperties nodeProperties,
            Logger logger
    ) {
        this.databaseService = databaseService;
        this.nodeProperties = nodeProperties;
        this.logger = logger;
    }

    @Override
    public void getNodeState(
            GetNodeStateRequest request,
            StreamObserver<GetNodeStateResponse> responseObserver) {

        System.out.println(request.getSender() + " asked for my state!");

        responseObserver.onNext(
                GetNodeStateResponse.newBuilder()
                        .setPort(nodeProperties.getPort())
                        .setNodeState(NodeState.HEAlTHY)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void createCollection(
            CreateCollectionRequest request,
            StreamObserver<CollectionMetaData> responseObserver
    ) {
        try {
            responseObserver.onNext(databaseService.createCollection(request.getName()));
            responseObserver.onCompleted();
        } catch (InvalidProtocolBufferException e) {
            var status = Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (CollectionAlreadyExistsException e) {
            var status = Status.INTERNAL
                    .withDescription("Collection " + request.getName() + " Already Exists!")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (IOException e) {
            logger.error("An IO Error occurred while creating collection " + request.getName());
            var status = Status.INTERNAL
                    .withDescription("An IO Error occurred while creating collection " + request.getName())
                    .withCause(e);
            responseObserver.onError(status.asException());
        }
    }

    @Override
    public void getCollections(
            GetCollectionsRequest request,
            StreamObserver<GetCollectionsResponse> responseObserver
    ) {
        responseObserver.onNext(
                GetCollectionsResponse.newBuilder()
                        .addAllCollectionsMetaData(databaseService.getCollections())
                        .build()
        );
        responseObserver.onCompleted();
    }
}
