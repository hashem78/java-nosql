package me.hashemalayan.services.grpc;

import btree4j.BTreeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.services.ClientCounterService;
import me.hashemalayan.services.db.DatabaseService;
import me.hashemalayan.services.db.IndexService;
import me.hashemalayan.services.db.exceptions.*;
import org.slf4j.Logger;

import java.io.IOException;

public class LocalNodeService extends NodeServiceGrpc.NodeServiceImplBase {

    private final DatabaseService databaseService;
    private final ClientCounterService clientCounterService;
    private final NodeProperties nodeProperties;

    private final IndexService indexService;

    private final Logger logger;

    @Inject
    public LocalNodeService(
            DatabaseService databaseService,
            ClientCounterService clientCounterService,
            NodeProperties nodeProperties,
            IndexService indexService,
            Logger logger
    ) {
        this.databaseService = databaseService;
        this.clientCounterService = clientCounterService;
        this.nodeProperties = nodeProperties;
        this.indexService = indexService;
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
                    databaseService.createCollection(
                            request.getName(),
                            request.getSchema()
                    )
            );
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
            e.printStackTrace();
        } catch (InvalidCollectionSchemaException e) {
            logger.error("Invalid Collection Schema " + request.getName() + " " + request.getSchema());
            var status = Status.INTERNAL
                    .withDescription("Invalid Collection Schema " + e.getMessage())
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
        } catch (CollectionDoesNotExistException e) {
            logger.error("User requested " + request.getCollectionId() + " but it does not exist");
            var status = Status.INTERNAL
                    .withDescription(request.getCollectionId() + "does not exist")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (IOException e) {
            var status = Status.INTERNAL
                    .withDescription("An IO Error occurred while creating collection " + request.getCollectionId())
                    .withCause(e);
            responseObserver.onError(status.asException());
        }
    }

    @Override
    public void editCollection(
            EditCollectionRequest request,
            StreamObserver<EditCollectionResponse> responseObserver
    ) {
        try {
            databaseService.editCollection(
                    request.getCollectionId(),
                    request.getCollectionName()
            );
            responseObserver.onNext(EditCollectionResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (CollectionDoesNotExistException e) {
            logger.error("User requested to edit" + request.getCollectionId() + " but it does not exist");
            var status = Status.INTERNAL
                    .withDescription(request.getCollectionId() + "does not exist")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (IOException e) {
            logger.error("An IO Error occurred while editing collection " + request.getCollectionId());
            var status = Status.INTERNAL
                    .withDescription("An IO Error occurred while editing collection " + request.getCollectionId())
                    .withCause(e);
            responseObserver.onError(status.asException());
        }
    }

    @Override
    public void deleteCollection(
            DeleteCollectionRequest request,
            StreamObserver<DeleteCollectionResponse> responseObserver
    ) {
        try {
            databaseService.deleteCollection(request.getCollectionId());
            responseObserver.onNext(
                    DeleteCollectionResponse.newBuilder()
                            .build()
            );
            responseObserver.onCompleted();
        } catch (CollectionDoesNotExistException e) {
            logger.error("User requested to delete" + request.getCollectionId() + " but it's already marked as deleted");
            var status = Status.INTERNAL
                    .withDescription(request.getCollectionId() + " does not exist")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (IOException e) {
            logger.error("An IO Error occurred while deleting collection " + request.getCollectionId());
            var status = Status.INTERNAL
                    .withDescription("An IO Error occurred while deleting collection " + request.getCollectionId())
                    .withCause(e);
            responseObserver.onError(status.asException());
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
        } catch (SampleMalformedException | JsonProcessingException e) {
            logger.error("User requested a sample for" + request.getCollectionId() + " but it's malformed");
            var status = Status.INTERNAL
                    .withDescription("Sample for " + request.getCollectionId() + " is malformed")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (CollectionDoesNotExistException e) {
            logger.error("User requested to get a sample for" + request.getCollectionId() + " but it's not found");
            var status = Status.INTERNAL
                    .withDescription(request.getCollectionId() + " does not exist")
                    .withCause(e);
            responseObserver.onError(status.asException());
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
        } catch (DocumentSchemaValidationException e) {
            logger.error(
                    "Failed to validate document's schema " + request.getCollectionId()
            );
            var status = Status.INTERNAL
                    .withDescription("Failed to validate schema")
                    .withCause(e);
            responseObserver.onError(status.asException());
            e.printStackTrace();
        } catch (CollectionDoesNotExistException e) {
            logger.error(
                    "User requested to edit/create a document on collection"
                            + request.getCollectionId()
                            + " but it's not found"
            );
            var status = Status.INTERNAL
                    .withDescription(request.getCollectionId() + " does not exist")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (IOException e) {
            logger.error("An IO Error occurred while editing collection " + request.getCollectionId());
            var status = Status.INTERNAL
                    .withDescription("An IO Error occurred while editing collection " + request.getCollectionId())
                    .withCause(e);
            responseObserver.onError(status.asException());
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCollectionDocument(
            DeleteCollectionDocumentRequest request,
            StreamObserver<DeleteCollectionDocumentResponse> responseObserver
    ) {
        try {
            databaseService.deleteDocument(request.getCollectionId(), request.getDocumentId());

            responseObserver.onNext(DeleteCollectionDocumentResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (CollectionDoesNotExistException e) {
            logger.error("User wants to delete "
                    + request.getDocumentId()
                    + " from collection" + request.getCollectionId()
                    + " But the collection doesn't exist"
            );
            var status = Status.INTERNAL
                    .withDescription("Collection" + request.getCollectionId() + " does not exist")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (DocumentDoesNotExistException e) {
            logger.error("User wants to delete "
                    + request.getDocumentId()
                    + " from collection" + request.getCollectionId()
                    + " But the document doesn't exist"
            );
            var status = Status.INTERNAL
                    .withDescription("Document "
                            + request.getDocumentId()
                            + "from collection" + request.getCollectionId()
                            + " does not exist"
                    )
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (IOException e) {
            logger.error("An IO Error occurred while delete document " + request);
            var status = Status.INTERNAL
                    .withDescription("An IO Error occurred while delete document " + request.getDocumentId())
                    .withCause(e);
            responseObserver.onError(status.asException());
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
            indexService.indexPropertyInCollection(request.getCollectionId(), request.getProperty());
            responseObserver.onNext(IndexCollectionPropertyResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (CollectionDoesNotExistException e) {
            logger.error("User requested " + request.getCollectionId() + " but it does not exist");
            var status = Status.INTERNAL
                    .withDescription(request.getCollectionId() + "does not exist")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (IOException | BTreeException e) {
            logger.error("An IO Error occurred while indexing a collection " + request);
            var status = Status.INTERNAL
                    .withDescription("Internal Server error")
                    .withCause(e);
            responseObserver.onError(status.asException());
        }
    }

    @Override
    public void isPropertyIndexed(
            IsPropertyIndexedRequest request,
            StreamObserver<IsPropertyIndexedResponse> responseObserver
    ) {
        final var isPropertyIndexed = indexService.isPropertyIndexed(
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
            indexService.removeIndexFromCollectionProperty(
                    request.getCollectionId(),
                    request.getProperty()
            );
            responseObserver.onNext(RemoveIndexFromCollectionPropertyResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (IndexNotFoundException e) {
            logger.error("User requested to remove an index on a non-indexed property " + request);
            var status = Status.INTERNAL
                    .withDescription("This property has no Index associated")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (CollectionDoesNotExistException e) {
            logger.error(
                    "User wanted to delete index on property "
                            + request.getProperty()
                            + " from " + request.getCollectionId()
                            + " but it does not exist"
            );
            var status = Status.INTERNAL
                    .withDescription(request.getCollectionId() + "does not exist")
                    .withCause(e);
            responseObserver.onError(status.asException());
        } catch (BTreeException | IOException e) {
            logger.error("An IO Error occurred while removing an index from a collection " + request);
            var status = Status.INTERNAL
                    .withDescription("Internal Server error")
                    .withCause(e);
            responseObserver.onError(status.asException());
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

        try {
            indexService.runQuery(
                    collectionId, operator,
                    property, value,
                    result -> {
                        responseObserver.onNext(
                                QueryDatabaseResponse.newBuilder()
                                        .setData(result)
                                        .build()
                        );
                    }
            );
            responseObserver.onCompleted();
        } catch (IndexNotFoundException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION
                            .withDescription(
                                    "Property " + property + " is not indexed in "
                                            + request.getCollectionId()
                            )
                            .asException()
            );
        } catch (BTreeException e) {
            throw new RuntimeException(e);
        } catch (UnRecognizedOperatorException e) {
            throw new RuntimeException(e);
        } catch (InvalidOperatorUsage e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
