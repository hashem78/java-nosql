package me.hashemalayan;

import com.google.inject.Guice;

public class Main {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("ERR: You have to supply a port for the gRPC server");
            System.exit(1);
        }
        var injector = Guice.createInjector(
                new EventLoopModule(),
                new NodeModule()
        );
        injector.getInstance(NodeEntryPoint.class).run();
    }
}

