package me.hashemalayan;

import btree4j.BTreeException;
import com.google.inject.Guice;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("ERR: You have to supply a port for the gRPC server");
            System.exit(1);
        }
        var injector = Guice.createInjector(
                new EventLoopModule(),
                new SignalingClientModule(),
                new NodeModule(args[0])
        );
        injector.getInstance(NodeEntryPoint.class).run(args[0]);
    }
}

