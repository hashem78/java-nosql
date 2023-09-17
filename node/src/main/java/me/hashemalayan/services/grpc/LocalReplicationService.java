package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.CreateCollectionReplicationMessage;
import me.hashemalayan.nosql.shared.EditCollectionReplicationMessage;
import me.hashemalayan.nosql.shared.ReplicationResponse;
import me.hashemalayan.nosql.shared.ReplicationServiceGrpc;
import me.hashemalayan.services.db.DatabaseService;

public class LocalReplicationService extends ReplicationServiceGrpc.ReplicationServiceImplBase {

    private final DatabaseService databaseService;

    @Inject
    public LocalReplicationService(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public void createCollection(
            CreateCollectionReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        try {
            databaseService.createCollection(request.getMetaData(), request.getSchema());
            responseObserver.onNext(ReplicationResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void editCollection(
            EditCollectionReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        try {
            databaseService.editCollection(request.getCollectionId(), request.getCollectionName());
            responseObserver.onNext(ReplicationResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
