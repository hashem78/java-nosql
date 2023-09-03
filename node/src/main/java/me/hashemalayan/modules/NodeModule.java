package me.hashemalayan.modules;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import me.hashemalayan.NodeProperties;
import me.hashemalayan.db.*;
import me.hashemalayan.loadbalancing.LoadBalancingService;
import me.hashemalayan.server.LocalNodeManager;
import me.hashemalayan.server.NodeService;
import me.hashemalayan.server.RemoteNodesManager;
import me.hashemalayan.signaling.SignalingStreamMeshObserverFactory;
import me.hashemalayan.util.JsonDirectoryIteratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DBSchemaLoader.class).to(BasicDBSchemaLoader.class);
        bind(NodeProperties.class);
        bind(SchemaManager.class).asEagerSingleton();
        bind(LocalNodeManager.class).asEagerSingleton();
        bind(RemoteNodesManager.class).asEagerSingleton();
        bind(DBManager.class).asEagerSingleton();
        bind(LoadBalancingService.class).asEagerSingleton();
        bind(LocalNodeManager.class).asEagerSingleton();
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
