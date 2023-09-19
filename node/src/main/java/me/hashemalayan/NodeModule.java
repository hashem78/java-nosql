package me.hashemalayan;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.hubspot.jackson.datatype.protobuf.ProtobufJacksonConfig;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import me.hashemalayan.factories.JsonDirectoryIteratorFactory;
import me.hashemalayan.factories.SignalingStreamMeshObserverFactory;
import me.hashemalayan.nosql.shared.LoadBalancingServiceGrpc.LoadBalancingServiceImplBase;
import me.hashemalayan.nosql.shared.NodeServiceGrpc.NodeServiceImplBase;
import me.hashemalayan.nosql.shared.ReplicationServiceGrpc.ReplicationServiceImplBase;
import me.hashemalayan.services.BasicClientCounterService;
import me.hashemalayan.services.ClientCounterService;
import me.hashemalayan.services.db.*;
import me.hashemalayan.services.db.interfaces.*;
import me.hashemalayan.services.grpc.*;
import me.hashemalayan.services.grpc.interfaces.LocalServicesManager;
import me.hashemalayan.services.grpc.interfaces.RemoteReplicationService;
import me.hashemalayan.services.grpc.interfaces.RemoteSignalingService;
import me.hashemalayan.util.BTreeCallbackFactory;
import me.hashemalayan.util.JsonSchemaDeserializer;
import me.hashemalayan.util.JsonSchemaSerializer;
import me.hashemalayan.util.interceptors.ExceptionHandlingInterceptor;
import me.hashemalayan.util.interceptors.LoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeModule extends AbstractModule {

    @Override
    protected void configure() {

        binder().requireAtInjectOnConstructors();
        binder().requireExactBindingAnnotations();
        binder().requireExplicitBindings();

        bind(NodeEntryPoint.class).asEagerSingleton();
        bind(NodeProperties.class).asEagerSingleton();
        bind(BTreeCallbackFactory.class).asEagerSingleton();
        bind(ExceptionHandlingInterceptor.class).asEagerSingleton();
        bind(LoggingInterceptor.class).asEagerSingleton();

        bind(RemoteReplicationService.class).to(BasicRemoteReplicationService.class).asEagerSingleton();
        bind(RemoteSignalingService.class).to(BasicRemoteSignalingService.class).asEagerSingleton();
        bind(LocalServicesManager.class).to(BasicLocalServicesManager.class).asEagerSingleton();
        bind(ClientCounterService.class).to(BasicClientCounterService.class).asEagerSingleton();

        bind(NodeServiceImplBase.class).to(LocalNodeService.class).asEagerSingleton();
        bind(LoadBalancingServiceImplBase.class).to(LoadBalancingService.class).asEagerSingleton();
        bind(ReplicationServiceImplBase.class).to(LocalReplicationService.class).asEagerSingleton();

        bind(CollectionConfigurationService.class).to(BasicCollectionConfigurationService.class).asEagerSingleton();
        bind(CollectionService.class).to(BasicCollectionService.class).asEagerSingleton();
        bind(IndexService.class).to(BasicIndexService.class).asEagerSingleton();
        bind(SampleFromSchemaService.class).to(BasicSampleFromSchemaService.class).asEagerSingleton();
        bind(SchemaService.class).to(BasicSchemaService.class).asEagerSingleton();

        bind(AbstractDatabaseService.class)
                .annotatedWith(Names.named("BroadcastingDbService"))
                .to(BroadcastingDatabaseService.class)
                .asEagerSingleton();

        bind(AbstractDatabaseService.class)
                .annotatedWith(Names.named("BasicDbService"))
                .to(BasicDatabaseService.class)
                .asEagerSingleton();

        install(
                new FactoryModuleBuilder()
                        .build(JsonDirectoryIteratorFactory.class)
        );

        install(
                new FactoryModuleBuilder()
                        .build(SignalingStreamMeshObserverFactory.class)
        );
    }

    @Provides
    Logger loggerProvider() {
        return LoggerFactory.getLogger("NodeLogger");
    }

    @Provides
    @Singleton
    ObjectMapper objectMapper() {

        ProtobufJacksonConfig.getDefaultInstance().acceptLiteralFieldnames();
        var module = new SimpleModule();
        module.addDeserializer(JsonSchema.class, new JsonSchemaDeserializer());
        module.addSerializer(JsonSchema.class, new JsonSchemaSerializer());

        return new ObjectMapper()
                .registerModule(new ProtobufModule())
                .registerModule(module);
    }

    @Provides
    JsonSchemaFactory jsonSchemaFactory() {
        return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }
}
