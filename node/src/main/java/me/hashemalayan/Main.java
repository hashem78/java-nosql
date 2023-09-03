package me.hashemalayan;

import com.google.inject.Guice;
import me.hashemalayan.modules.NodeModule;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Main {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
    public static void main(String[] args) {

        var injector = Guice.createInjector(new NodeModule());
        injector.getInstance(NodeEntryPoint.class).run();
    }
}

