package me.hashemalayan;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.services.*;
import me.hashemalayan.factories.SignalingStreamMeshObserverFactory;
import me.hashemalayan.factories.JsonDirectoryIteratorFactory;
import me.hashemalayan.services.interfaces.SchemaLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SchemaLoader.class).to(SchemaLoaderService.class);
        bind(NodeProperties.class).asEagerSingleton();
        bind(SchemaService.class).asEagerSingleton();
        bind(LocalNodeService.class).asEagerSingleton();
        bind(RemoteNodesService.class).asEagerSingleton();
        bind(DatabaseService.class).asEagerSingleton();
        bind(LoadBalancingService.class).asEagerSingleton();
        bind(LocalNodeService.class).asEagerSingleton();
        bind(NodeService.class).asEagerSingleton();

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
        return new ObjectMapper();
    }

    @Provides
    JsonSchemaFactory jsonSchemaFactory() {
        return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    }
}
