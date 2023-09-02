package me.hashemalayan;

import com.google.inject.Guice;
import io.grpc.ServerBuilder;
import me.hashemalayan.signaling.SignalingServerImpl;
import me.hashemalayan.signaling.SignalingServerModule;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        var injector = Guice.createInjector(
                new SignalingServerModule()
        );
        var server = ServerBuilder.forPort(8000)
                .addService(injector.getInstance(SignalingServerImpl.class))
                .build();

        server.start();
        server.awaitTermination();
    }
}