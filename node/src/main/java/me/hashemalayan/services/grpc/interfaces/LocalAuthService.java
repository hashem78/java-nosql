package me.hashemalayan.services.grpc.interfaces;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.*;
import me.hashemalayan.services.auth.AuthContext;

public class LocalAuthService extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthContext authContext;

    @Inject
    public LocalAuthService(AuthContext authContext) {
        this.authContext = authContext;
    }

    @Override
    public void registerUser(
            UserRegistrationRequest request,
            StreamObserver<User> responseObserver
    ) {
        responseObserver.onNext(authContext.registerUser(request.getEmail(), request.getPassword()));
        responseObserver.onCompleted();
    }

    @Override
    public void getCredentials(GetUserCredentialsRequest request, StreamObserver<User> responseObserver) {
        responseObserver.onNext(authContext.getCredentials(request.getEmail(), request.getPassword()));
        responseObserver.onCompleted();
    }

    @Override
    public void authenticateUser(
            UserAuthenticationRequest request,
            StreamObserver<UserAuthenticationResponse> responseObserver
    ) {

        responseObserver.onNext(
                UserAuthenticationResponse.newBuilder()
                        .setToken(authContext.authenticateUser(request.getEmail(), request.getPassword()))
                        .build()
        );
        responseObserver.onCompleted();
    }
}
