package me.hashemalayan;

import com.google.inject.Guice;

public class Main {
    public static void main(String[] args) {
        System.setProperty("io.grpc.netty.shaded.io.netty.log.level", "DEBUG");

        if(args.length != 1)
        {
            System.err.println("ERR: You have to supply a port for the gRPC server");
            System.exit(1);
        }
        var injector = Guice.createInjector(
                new EventLoopModule(),
                new SignalingClientModule(),
                new NodeModule()
        );
        System.out.println("Starting on port: " + args[0]);
        injector.getInstance(NodeEntryPoint.class).run(args[0]);
    }
}

