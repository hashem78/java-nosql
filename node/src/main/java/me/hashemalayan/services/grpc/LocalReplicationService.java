package me.hashemalayan.services.grpc;

import btree4j.BTreeException;
import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.services.db.DatabaseService;
import me.hashemalayan.services.db.exceptions.AffinityMismatchException;
import me.hashemalayan.services.db.exceptions.CollectionDoesNotExistException;
import me.hashemalayan.services.db.exceptions.DocumentSchemaValidationException;
import me.hashemalayan.services.db.exceptions.IndexNotFoundException;

import java.io.IOException;

import static me.hashemalayan.nosql.shared.Common.*;

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

    @Override
    public void deleteCollection(
            DeleteCollectionReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        try {
            databaseService.deleteCollection(request.getCollectionId());
            responseObserver.onNext(ReplicationResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setCollectionDocument(
            SetDocumentReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        try {
             databaseService.setDocument(
                    request.getCollectionId(),
                    request.getDocument()
            );
            responseObserver.onNext(ReplicationResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void collectionDocumentRedirection(
            SetCollectionDocumentRequest request,
            StreamObserver<CollectionDocument> responseObserver
    ) {
        try {
            final var document = databaseService.setDocument(
                    request.getCollectionId(),
                    request.getDocumentId(),
                    request.getDocument()
            );
            responseObserver.onNext(document);
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCollectionDocument(
            DeleteDocumentReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        try {
            databaseService.deleteDocument(
                    request.getCollectionId(),
                    request.getDocumentId()
            );
            responseObserver.onNext(ReplicationResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
