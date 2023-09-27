package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.nosql.shared.Common.SetCollectionDocumentResponse;
import me.hashemalayan.services.db.exceptions.DocumentOptimisticLockException;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;
import me.hashemalayan.services.grpc.interfaces.RemoteReplicationService;

import static me.hashemalayan.nosql.shared.Common.CollectionDocument;
import static me.hashemalayan.nosql.shared.Common.SetCollectionDocumentRequest;

public class LocalReplicationService extends ReplicationServiceGrpc.ReplicationServiceImplBase {

    private final AbstractDatabaseService databaseService;

    private final RemoteReplicationService replicationService;

    @Inject
    public LocalReplicationService(
            @Named("BasicDbService") AbstractDatabaseService databaseService,
            RemoteReplicationService replicationService) {
        this.databaseService = databaseService;
        this.replicationService = replicationService;
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
        databaseService.editCollection(request.getCollectionId(), request.getCollectionName());
        responseObserver.onNext(ReplicationResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteCollection(
            DeleteCollectionReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {

        databaseService.deleteCollection(request.getCollectionId());
        responseObserver.onNext(ReplicationResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void setCollectionDocument(
            SetDocumentReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {

        databaseService.setDocument(
                request.getCollectionId(),
                request.getDocument()
        );
        responseObserver.onNext(ReplicationResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void collectionDocumentRedirection(
            SetCollectionDocumentRequest request,
            StreamObserver<SetCollectionDocumentResponse> responseObserver
    ) {
        try {
            final var document = databaseService.setDocument(
                    request.getCollectionId(),
                    request.getDocumentId(),
                    request.getDocument()
            );

            replicationService.broadcast(
                    ReplicationMessage.newBuilder()
                            .setSetDocumentReplicationMessage(
                                    SetDocumentReplicationMessage.newBuilder()
                                            .setCollectionId(request.getCollectionId())
                                            .setDocument(document)
                                            .build()
                            )
                            .build()
            );

            responseObserver.onNext(
                    SetCollectionDocumentResponse.newBuilder()
                            .setDocument(document)
                            .build()
            );
            responseObserver.onCompleted();
        } catch (DocumentOptimisticLockException e) {
            responseObserver.onNext(
                    SetCollectionDocumentResponse.newBuilder()
                            .setRetrySetCollection(Common.RetrySetCollectionResponse.newBuilder().build())
                            .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteCollectionDocument(
            DeleteDocumentReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        databaseService.deleteDocument(
                request.getCollectionId(),
                request.getDocumentId()
        );
        responseObserver.onNext(ReplicationResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void indexCollectionProperty(
            IndexCollectionPropertyReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        databaseService.indexPropertyInCollection(
                request.getCollectionId(),
                request.getProperty()
        );
        responseObserver.onNext(ReplicationResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeIndexFromCollectionProperty(
            RemoveIndexReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        databaseService.removeIndexFromCollectionProperty(
                request.getCollectionId(),
                request.getProperty()
        );
        responseObserver.onNext(ReplicationResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void compoundIndex(
            CompoundIndexReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        databaseService.compoundIndex(request.getCollectionId(), request.getPropertiesList());
        responseObserver.onNext(ReplicationResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeCompoundIndex(
            RemoveCompoundIndexReplicationMessage request,
            StreamObserver<ReplicationResponse> responseObserver
    ) {
        databaseService.removeCompoundIndex(request.getCollectionId(), request.getPropertiesList());
        responseObserver.onNext(ReplicationResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
