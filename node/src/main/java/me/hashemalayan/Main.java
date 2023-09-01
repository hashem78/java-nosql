package me.hashemalayan;

import com.google.inject.Guice;
import me.hashemalayan.modules.NodeModule;

public class Main {
    public static void main(String[] args) {

        var injector = Guice.createInjector(
                new EventLoopModule(),
                new NodeModule()
        );
        injector.getInstance(NodeEntryPoint.class).run();
    }
}

