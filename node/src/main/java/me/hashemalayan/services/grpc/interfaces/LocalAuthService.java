package me.hashemalayan.services.grpc.interfaces;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import me.hashemalayan.nosql.shared.AuthServiceGrpc;
import me.hashemalayan.nosql.shared.User;
import me.hashemalayan.nosql.shared.UserRegistrationRequest;
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
}
