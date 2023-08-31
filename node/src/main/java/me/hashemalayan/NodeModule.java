package me.hashemalayan;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import me.hashemalayan.db.DBManager;
import me.hashemalayan.server.LocalNodeManager;
import me.hashemalayan.server.RemoteNodesManager;
import me.hashemalayan.signaling.SignalingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LocalNodeManager.class).asEagerSingleton();
        bind(RemoteNodesManager.class).asEagerSingleton();
        bind(DBManager.class).asEagerSingleton();
        bind(NodeProperties.class).asEagerSingleton();
        bind(SignalingClient.class).asEagerSingleton();
    }

    @Provides
    Logger loggerProvider() {
        return LoggerFactory.getLogger("NodeLogger");
    }

    @Provides @Singleton
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
