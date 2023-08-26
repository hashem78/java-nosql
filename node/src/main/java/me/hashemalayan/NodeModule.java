package me.hashemalayan;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import me.hashemalayan.server.LocalNodeManager;
import me.hashemalayan.server.RemoteNodesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LocalNodeManager.class).asEagerSingleton();
        bind(RemoteNodesManager.class).asEagerSingleton();
    }

    @Provides
    Logger loggerProvider() {
        return LoggerFactory.getLogger("NodeLogger");
    }
}
