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
import me.hashemalayan.services.ClientCounterService;
import me.hashemalayan.services.db.*;
import me.hashemalayan.services.db.interfaces.AbstractDatabaseService;
import me.hashemalayan.services.db.interfaces.CollectionConfigurationService;
import me.hashemalayan.services.grpc.*;
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
        bind(RemoteSignalingService.class).asEagerSingleton();
        bind(NodeProperties.class).asEagerSingleton();
        bind(SchemaService.class).asEagerSingleton();
        bind(LocalServicesManager.class).asEagerSingleton();
        bind(AbstractDatabaseService.class)
                .annotatedWith(Names.named("BroadcastingDbService"))
                .to(BroadcastingDatabaseService.class)
                .asEagerSingleton();
        bind(AbstractDatabaseService.class)
                .annotatedWith(Names.named("BasicDbService"))
                .to(DatabaseService.class)
                .asEagerSingleton();
        bind(LoadBalancingService.class).asEagerSingleton();
        bind(LocalServicesManager.class).asEagerSingleton();
        bind(LocalNodeService.class).asEagerSingleton();
        bind(CollectionConfigurationService.class).to(BasicCollectionConfigurationService.class).asEagerSingleton();
        bind(CollectionService.class).asEagerSingleton();
        bind(SampleFromSchemaService.class).asEagerSingleton();
        bind(ClientCounterService.class).asEagerSingleton();
        bind(IndexService.class).asEagerSingleton();
        bind(BTreeCallbackFactory.class).asEagerSingleton();
        bind(ExceptionHandlingInterceptor.class).asEagerSingleton();
        bind(LoggingInterceptor.class).asEagerSingleton();
        bind(LocalReplicationService.class).asEagerSingleton();
        bind(RemoteReplicationService.class).asEagerSingleton();

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
