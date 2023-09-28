package me.hashemalayan.util.interceptors;

import com.google.inject.Inject;
import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import me.hashemalayan.nosql.shared.AuthServiceGrpc;
import me.hashemalayan.nosql.shared.LoadBalancingServiceGrpc;
import me.hashemalayan.nosql.shared.SignalingServiceGrpc;
import me.hashemalayan.util.Constants;

import java.util.Objects;

import static me.hashemalayan.util.Constants.JWT_SIGNING_KEY;

public class JwtInterceptor implements ServerInterceptor {

    private final JwtParser parser = Jwts.parserBuilder().setSigningKey(JWT_SIGNING_KEY.getBytes()).build();

    @Inject
    public JwtInterceptor() {

    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler
    ) {

        final var serviceName = serverCall.getMethodDescriptor().getServiceName();
        if (Objects.equals(serviceName, SignalingServiceGrpc.SERVICE_NAME))
            return serverCallHandler.startCall(serverCall, metadata);
        else if (Objects.equals(serviceName, LoadBalancingServiceGrpc.SERVICE_NAME)) {
            return serverCallHandler.startCall(serverCall, metadata);
        } else if (Objects.equals(serviceName, AuthServiceGrpc.SERVICE_NAME)) {
            return serverCallHandler.startCall(serverCall, metadata);
        }

        String value = metadata.get(Constants.AUTHORIZATION_METADATA_KEY);

        Status status = Status.OK;
        if (value == null) {
            status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing");
        } else if (!value.startsWith(Constants.BEARER_TYPE)) {
            status = Status.UNAUTHENTICATED.withDescription("Unknown authorization type");
        } else {
            Jws<Claims> claims = null;
            String token = value.substring(Constants.BEARER_TYPE.length()).trim();
            try {

                claims = parser.parseClaimsJws(token);
            } catch (JwtException e) {
                status = Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e);
            }
            if (claims != null) {

                Context ctx = Context.current()
                        .withValue(Constants.CLIENT_ID_CONTEXT_KEY, claims.getBody().getSubject());
                return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
            }
        }

        serverCall.close(status, new Metadata());
        return new ServerCall.Listener<>() {
        };
    }

}