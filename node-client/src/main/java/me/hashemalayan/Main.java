package me.hashemalayan;

import io.grpc.ManagedChannelBuilder;
import me.hashemalayan.nosql.shared.HelloRequest;
import me.hashemalayan.nosql.shared.HelloServiceGrpc;

public class Main {
    public static void main(String[] args) {
        var channel = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        var stub = HelloServiceGrpc.newBlockingStub(channel);
        var request = HelloRequest.newBuilder().setName("Alice").build();
        var response = stub.sayHello(request);

        System.out.println(response.getMessage());

        channel.shutdown();
    }
}