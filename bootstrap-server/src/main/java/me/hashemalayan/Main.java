package me.hashemalayan;


import io.grpc.ServerBuilder;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        final var properties = new BootstrapProperties();
        final var serverBuilder = ServerBuilder.forPort(properties.getPort());

        if (properties.isUseSSL()) {
            System.out.println("Using SSL");
            serverBuilder.useTransportSecurity(
                    properties.getSslCertificatePath().toFile(),
                    properties.getPrivateKeyPath().toFile()
            );
        }

        final var server = serverBuilder
                .addService(new SignalingServer(properties))
                .build();
        server.start();
        server.awaitTermination();
    }
}