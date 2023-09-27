package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;
import me.hashemalayan.nosql.shared.Common.SetCollectionDocumentRequest;
import me.hashemalayan.nosql.shared.Common.SetCollectionDocumentResponse;
import me.hashemalayan.services.ClientCounterService;
import me.hashemalayan.services.db.exceptions.AffinityMismatchException;
import me.hashemalayan.services.db.exceptions.DocumentOptimisticLockException;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;
import me.hashemalayan.services.grpc.interfaces.RemoteReplicationService;
import org.slf4j.Logger;

import static me.hashemalayan.nosql.shared.Common.*;

public class LocalNodeService extends NodeServiceGrpc.NodeServiceImplBase {

    private final AbstractDatabaseService databaseService;
    private final RemoteReplicationService replicationService;
    private final ClientCounterService clientCounterService;
    private final NodeProperties nodeProperties;
    private final Logger logger;

    @Inject
    public LocalNodeService(
            @Named("BroadcastingDbService") AbstractDatabaseService databaseService,
            RemoteReplicationService replicationService,
            ClientCounterService clientCounterService,
            NodeProperties nodeProperties,
            Logger logger
    ) {
        this.databaseService = databaseService;
        this.replicationService = replicationService;
        this.clientCounterService = clientCounterService;
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

        responseObserver.onNext(
                databaseService.createCollection(
                        request.getName(),
                        request.getSchema()
                )
        );
        responseObserver.onCompleted();
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

    @Override
    public void getCollectionDocuments(
            GetCollectionDocumentsRequest request,
            StreamObserver<CollectionDocument> responseObserver
    ) {

        databaseService.getDocuments(
                request.getCollectionId(),
                responseObserver::onNext
        );
        responseObserver.onCompleted();
    }

    @Override
    public void editCollection(
            EditCollectionRequest request,
            StreamObserver<EditCollectionResponse> responseObserver
    ) {
        databaseService.editCollection(
                request.getCollectionId(),
                request.getCollectionName()
        );
        responseObserver.onNext(EditCollectionResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteCollection(
            DeleteCollectionRequest request,
            StreamObserver<DeleteCollectionResponse> responseObserver
    ) {
        databaseService.deleteCollection(request.getCollectionId());
        responseObserver.onNext(
                DeleteCollectionResponse.newBuilder()
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void getDocumentSample(
            GetDocumentSampleRequest request,
            StreamObserver<GetDocumentSampleResponse> responseObserver
    ) {

        var sample = databaseService.getDocumentSample(request.getCollectionId());
        responseObserver.onNext(
                GetDocumentSampleResponse.newBuilder()
                        .setDocumentSample(sample)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void setCollectionDocument(
            SetCollectionDocumentRequest request,
            StreamObserver<SetCollectionDocumentResponse> responseObserver
    ) {
        try {
            final var document = databaseService.setDocument(
                    request.getCollectionId(),
                    request.getDocumentId(),
                    request.getDocument()
            );
            responseObserver.onNext(
                    SetCollectionDocumentResponse.newBuilder()
                            .setDocument(document)
                            .build()
            );
            responseObserver.onCompleted();
        } catch (AffinityMismatchException e) {
            responseObserver.onNext(
                    SetCollectionDocumentResponse.newBuilder()
                            .setDocument(replicationService.redirect(e.getExpectedAffinity(), request))
                            .build()
            );
            responseObserver.onCompleted();
        } catch (DocumentOptimisticLockException e) {
            responseObserver.onNext(
                    SetCollectionDocumentResponse.newBuilder()
                            .setRetrySetCollection(RetrySetCollectionResponse.newBuilder().build())
                            .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteCollectionDocument(
            DeleteCollectionDocumentRequest request,
            StreamObserver<DeleteCollectionDocumentResponse> responseObserver
    ) {

        databaseService.deleteDocument(request.getCollectionId(), request.getDocumentId());
        responseObserver.onNext(DeleteCollectionDocumentResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ClientHelloRequest> clientHello(
            StreamObserver<ClientHelloResponse> responseObserver
    ) {
        return new StreamObserver<>() {
            @Override
            public void onNext(ClientHelloRequest value) {
                logger.info("Client Connected");
                clientCounterService.increment();
                responseObserver.onNext(ClientHelloResponse.newBuilder().build());
            }

            @Override
            public void onError(Throwable t) {
                logger.info("Client forcefully disconnected");
                clientCounterService.decrement();
            }

            @Override
            public void onCompleted() {
                logger.info("Client safely disconnected");
                clientCounterService.decrement();
            }
        };
    }

    @Override
    public void indexCollectionProperty(
            IndexCollectionPropertyRequest request,
            StreamObserver<IndexCollectionPropertyResponse> responseObserver
    ) {

        logger.info("Indexing " + request.getProperty() + " in collection " + request.getCollectionId());
        databaseService.indexPropertyInCollection(request.getCollectionId(), request.getProperty());
        responseObserver.onNext(IndexCollectionPropertyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void isPropertyIndexed(
            IsPropertyIndexedRequest request,
            StreamObserver<IsPropertyIndexedResponse> responseObserver
    ) {
        final var isPropertyIndexed = databaseService.isPropertyIndexed(
                request.getCollectionId(),
                request.getProperty()
        );
        responseObserver.onNext(
                IsPropertyIndexedResponse.newBuilder()
                        .setIsIndexed(isPropertyIndexed)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void removeIndexFromCollectionProperty(
            RemoveIndexFromCollectionPropertyRequest request,
            StreamObserver<RemoveIndexFromCollectionPropertyResponse> responseObserver
    ) {

        databaseService.removeIndexFromCollectionProperty(
                request.getCollectionId(),
                request.getProperty()
        );
        responseObserver.onNext(RemoveIndexFromCollectionPropertyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getCollectionMetaData(
            GetCollectionMetaDataRequest request,
            StreamObserver<CollectionMetaData> responseObserver
    ) {
        final var metaDataOpt = databaseService.getCollectionMetaData(request.getCollectionId());
        if (metaDataOpt.isPresent()) {
            responseObserver.onNext(metaDataOpt.get());
            responseObserver.onCompleted();
        } else {
            logger.error("User requested to get metadata of collection" + request.getCollectionId() + " but it's not present");
            var status = Status.NOT_FOUND
                    .withDescription(request.getCollectionId() + " does not exist");
            responseObserver.onError(status.asException());
        }
    }

    @Override
    public void queryDatabase(
            QueryDatabaseRequest request,
            StreamObserver<QueryDatabaseResponse> responseObserver
    ) {

        databaseService.runQuery(
                request.getCollectionId(),
                request.getOperator(),
                request.getProperty(),
                request.getValue(),
                result -> responseObserver.onNext(
                        QueryDatabaseResponse.newBuilder()
                                .setData(result)
                                .build()
                )
        );
        responseObserver.onCompleted();
    }

    @Override
    public void getCollectionDocument(
            GetCollectionDocumentRequest request,
            StreamObserver<CollectionDocument> responseObserver
    ) {

        responseObserver.onNext(
                databaseService.getDocument(
                        request.getCollectionId(),
                        request.getDocumentId()
                )
        );
        responseObserver.onCompleted();
    }

    @Override
    public void getPropertyType(
            GetCollectionPropertyTypeRequest request,
            StreamObserver<GetCollectionPropertyTypeResponse> responseObserver
    ) {
        final var propertyType = databaseService.getPropertyType(
                request.getCollectionId(),
                request.getProperty()
        );
        responseObserver.onNext(
                GetCollectionPropertyTypeResponse.newBuilder()
                        .setPropertyType(propertyType)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void compoundIndex(
            CompoundIndexRequest request,
            StreamObserver<IndexCollectionPropertyResponse> responseObserver
    ) {
        databaseService.compoundIndex(request.getCollectionId(), request.getPropertiesList());
        responseObserver.onNext(IndexCollectionPropertyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void compoundQuery(
            CompoundIndexQueryRequest request,
            StreamObserver<QueryDatabaseResponse> responseObserver
    ) {

        databaseService.compoundQuery(
                request.getCollectionId(),
                request.getOperator(),
                request.getQuery(),
                result -> responseObserver.onNext(
                        QueryDatabaseResponse.newBuilder()
                                .setData(result)
                                .build()
                )
        );
        responseObserver.onCompleted();
    }

    @Override
    public void compoundQuerySync(
            CompoundIndexQueryRequest request,
            StreamObserver<CompoundIndexQueryResponse> responseObserver
    ) {

        final var queryResponse = databaseService.compoundQuery(
                request.getCollectionId(),
                request.getOperator(),
                request.getQuery()
        );
        responseObserver.onNext(
                CompoundIndexQueryResponse.newBuilder()
                        .addAllDocumentIds(queryResponse)
                .build()
        );
        responseObserver.onCompleted();
    }
}