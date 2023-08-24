package me.hashemalayan.services;

import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.HelloRequest;
import me.hashemalayan.nosql.shared.HelloResponse;
import me.hashemalayan.nosql.shared.HelloServiceGrpc;

public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage("Hello, " + request.getName() + "!")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}