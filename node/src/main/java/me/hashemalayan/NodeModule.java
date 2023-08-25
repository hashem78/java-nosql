package me.hashemalayan;

import com.google.inject.AbstractModule;
import me.hashemalayan.server.LocalNodeManager;
import me.hashemalayan.server.RemoteNodesManager;

public class NodeModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(LocalNodeManager.class).asEagerSingleton();
        bind(RemoteNodesManager.class).asEagerSingleton();
    }
}
