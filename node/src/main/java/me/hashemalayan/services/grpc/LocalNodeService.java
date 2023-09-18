package me.hashemalayan.services.grpc;

import com.google.inject.Inject;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.nosql.shared.Common.CollectionDocument;
import me.hashemalayan.nosql.shared.Common.CollectionMetaData;
import me.hashemalayan.nosql.shared.Common.SetCollectionDocumentRequest;
import me.hashemalayan.nosql.shared.Common.SetCollectionDocumentResponse;
import me.hashemalayan.services.ClientCounterService;
import me.hashemalayan.services.db.DatabaseService;
import me.hashemalayan.services.db.exceptions.AffinityMismatchException;
import org.slf4j.Logger;

public class LocalNodeService extends NodeServiceGrpc.NodeServiceImplBase {

    private final DatabaseService databaseService;

    private final RemoteReplicationService replicationService;
    private final ClientCounterService clientCounterService;
    private final NodeProperties nodeProperties;
    private final Logger logger;

    @Inject
    public LocalNodeService(
            DatabaseService databaseService,
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
        try {
            responseObserver.onNext(
                    databaseService.createCollectionAndBroadcast(
                            request.getName(),
                            request.getSchema()
                    )
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    @Override
    public void getCollectionDocuments(
            GetCollectionDocumentsRequest request,
            StreamObserver<CollectionDocument> responseObserver
    ) {
        try {
            databaseService.getDocuments(
                    request.getCollectionId(),
                    responseObserver::onNext
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void editCollection(
            EditCollectionRequest request,
            StreamObserver<EditCollectionResponse> responseObserver
    ) {
        try {
            databaseService.editCollectionAndBroadcast(
                    request.getCollectionId(),
                    request.getCollectionName()
            );
            responseObserver.onNext(EditCollectionResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCollection(
            DeleteCollectionRequest request,
            StreamObserver<DeleteCollectionResponse> responseObserver
    ) {
        try {
            databaseService.deleteCollectionAndBroadcast(request.getCollectionId());
            responseObserver.onNext(
                    DeleteCollectionResponse.newBuilder()
                            .build()
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getDocumentSample(
            GetDocumentSampleRequest request,
            StreamObserver<GetDocumentSampleResponse> responseObserver
    ) {
        try {
            var sample = databaseService.getDocumentSample(request.getCollectionId());
            responseObserver.onNext(
                    GetDocumentSampleResponse.newBuilder()
                            .setDocumentSample(sample)
                            .build()
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCollectionDocument(
            DeleteCollectionDocumentRequest request,
            StreamObserver<DeleteCollectionDocumentResponse> responseObserver
    ) {
        try {
            databaseService.deleteDocumentAndBroadcast(request.getCollectionId(), request.getDocumentId());

            responseObserver.onNext(DeleteCollectionDocumentResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        try {
            logger.info("Indexing " + request.getProperty() + " in collection " + request.getCollectionId());
            databaseService.indexPropertyInCollectionAndBroadcast(request.getCollectionId(), request.getProperty());
            responseObserver.onNext(IndexCollectionPropertyResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        try {
            databaseService.removeIndexFromCollectionProperty(
                    request.getCollectionId(),
                    request.getProperty()
            );
            responseObserver.onNext(RemoveIndexFromCollectionPropertyResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            var status = Status.INTERNAL
                    .withDescription(request.getCollectionId() + " does not exist");
            responseObserver.onError(status.asException());
        }
    }

    @Override
    public void queryDatabase(
            QueryDatabaseRequest request,
            StreamObserver<QueryDatabaseResponse> responseObserver
    ) {
        final var collectionId = request.getCollectionId();
        final var property = request.getProperty();
        final var operator = request.getOperator();
        final var value = request.getValue();
        logger.info("Query Request: \n" + request);

        try {
            databaseService.runQuery(
                    collectionId, operator,
                    property,
                    value,
                    result -> responseObserver.onNext(
                            QueryDatabaseResponse.newBuilder()
                                    .setData(result)
                                    .build()
                    )
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getCollectionDocument(
            GetCollectionDocumentRequest request,
            StreamObserver<CollectionDocument> responseObserver
    ) {
        try {
            responseObserver.onNext(
                    databaseService.getDocument(
                            request.getCollectionId(),
                            request.getDocumentId()
                    )
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getPropertyType(
            GetCollectionPropertyTypeRequest request,
            StreamObserver<GetCollectionPropertyTypeResponse> responseObserver
    ) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
