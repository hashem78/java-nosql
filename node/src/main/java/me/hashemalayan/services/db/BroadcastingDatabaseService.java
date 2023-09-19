package me.hashemalayan.services.db;

import btree4j.BTreeException;
import com.google.inject.Inject;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.services.db.exceptions.*;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;
import me.hashemalayan.services.db.interfaces.CollectionService;
import me.hashemalayan.services.db.interfaces.IndexService;
import me.hashemalayan.services.db.interfaces.SchemaService;
import me.hashemalayan.services.grpc.RemoteReplicationService;

import java.io.IOException;

public class BroadcastingDatabaseService extends AbstractDatabaseService {
    private final RemoteReplicationService replicationService;
    @Inject
    public BroadcastingDatabaseService(
            CollectionService collectionService,
            SchemaService schemaService,
            IndexService indexService,
            RemoteReplicationService replicationService
    ) {
        super(collectionService, schemaService, indexService);
        this.replicationService = replicationService;
    }

    @Override
    public Common.CollectionMetaData createCollection(String collectionName, String schema)
            throws IOException,
            CollectionAlreadyExistsException,
            InvalidCollectionSchemaException {
        final var metaData = super.createCollection(collectionName, schema);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setCreateCollectionReplicationMessage(
                                CreateCollectionReplicationMessage.newBuilder()
                                        .setMetaData(metaData)
                                        .setSchema(schema)
                                        .build()
                        )
                        .build()
        );
        return metaData;
    }

    public Common.CollectionDocument setDocument(
            String collectionId,
            String documentId,
            String documentJson
    ) throws DocumentSchemaValidationException,
            CollectionDoesNotExistException,
            IOException,
            BTreeException,
            IndexNotFoundException,
            AffinityMismatchException {

        final var document = super.setDocument(collectionId, documentId, documentJson);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setSetDocumentReplicationMessage(
                                SetDocumentReplicationMessage.newBuilder()
                                        .setCollectionId(collectionId)
                                        .setDocument(document)
                                        .build()
                        )
                        .build()
        );
        return document;
    }

    public void editCollection(
            String collectionId,
            String collectionName
    ) throws CollectionDoesNotExistException,
            IOException {
        super.editCollection(collectionId, collectionName);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setEditCollectionReplicationMessage(
                                EditCollectionReplicationMessage.newBuilder()
                                        .setCollectionId(collectionId)
                                        .setCollectionName(collectionName)
                                        .build()
                        )
                        .build()
        );
    }


    public void deleteCollection(String collectionId) throws CollectionDoesNotExistException, IOException {
        super.deleteCollection(collectionId);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setDeleteCollectionReplicationMessage(
                                DeleteCollectionReplicationMessage.newBuilder()
                                        .setCollectionId(collectionId)
                                        .build()
                        )
                        .build()
        );
    }

    public void deleteDocument(String collectionId, String documentId) throws
            CollectionDoesNotExistException,
            DocumentDoesNotExistException,
            IOException {
        super.deleteDocument(collectionId, documentId);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setDeleteDocumentReplicationMessage(
                                DeleteDocumentReplicationMessage.newBuilder()
                                        .setCollectionId(collectionId)
                                        .setDocumentId(documentId)
                                        .build()
                        )
                        .build()
        );
    }

    public void indexPropertyInCollection(String collectionId, String property) throws
            IOException,
            BTreeException,
            CollectionDoesNotExistException {
        super.indexPropertyInCollection(collectionId, property);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setIndexCollectionPropertyReplicationMessage(
                                IndexCollectionPropertyReplicationMessage.newBuilder()
                                        .setCollectionId(collectionId)
                                        .setProperty(property)
                                        .build()
                        )
                        .build()
        );
    }

    public void removeIndexFromCollectionProperty(String collectionId, String property)
            throws IndexNotFoundException,
            BTreeException,
            IOException, CollectionDoesNotExistException {
        super.removeIndexFromCollectionProperty(collectionId, property);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setRemoveIndexReplicationMessage(
                                RemoveIndexReplicationMessage.newBuilder()
                                        .setCollectionId(collectionId)
                                        .setProperty(property)
                                        .build()
                        )
                        .build()
        );
    }
}
