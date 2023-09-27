package me.hashemalayan.services.db;

import com.google.inject.Inject;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;
import me.hashemalayan.services.db.interfaces.CollectionService;
import me.hashemalayan.services.db.interfaces.IndexService;
import me.hashemalayan.services.db.interfaces.SchemaService;
import me.hashemalayan.services.grpc.interfaces.RemoteReplicationService;
import me.hashemalayan.util.CustomStructToJson;

import java.util.List;

public class BroadcastingDatabaseService extends AbstractDatabaseService {
    private final RemoteReplicationService replicationService;
    @Inject
    public BroadcastingDatabaseService(
            CollectionService collectionService,
            SchemaService schemaService,
            IndexService indexService,
            RemoteReplicationService replicationService,
            CustomStructToJson customStructToJson
    ) {
        super(collectionService, schemaService, indexService, customStructToJson);
        this.replicationService = replicationService;
    }

    @Override
    public Common.CollectionMetaData createCollection(String collectionName, String schema) {
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
    )  {

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
    )  {
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


    public void deleteCollection(String collectionId) {
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

    public void deleteDocument(String collectionId, String documentId){
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

    public void indexPropertyInCollection(String collectionId, String property) {
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

    public void removeIndexFromCollectionProperty(String collectionId, String property) {
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

    @Override
    public void compoundIndex(String collectionId, List<String> properties) {
        super.compoundIndex(collectionId, properties);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setCompoundIndexReplicationMessage(
                                CompoundIndexReplicationMessage.newBuilder()
                                        .setCollectionId(collectionId)
                                        .addAllProperties(properties)
                                        .build()
                        )
                        .build()
        );
    }

    @Override
    public void removeCompoundIndex(String collectionId, List<String> properties) {
        super.removeCompoundIndex(collectionId, properties);
        replicationService.broadcast(
                ReplicationMessage.newBuilder()
                        .setRemoveCompoundIndexReplicationMessage(
                                RemoveCompoundIndexReplicationMessage.newBuilder()
                                        .setCollectionId(collectionId)
                                        .addAllProperties(properties)
                                        .build()
                        )
                        .build()
        );
    }
}
