package me.hashemalayan.util.interceptors;


import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.*;
import me.hashemalayan.services.auth.exceptions.UserAlreadyExistsException;
import me.hashemalayan.services.auth.exceptions.UserDoesNotExistException;
import me.hashemalayan.services.db.exceptions.*;

import java.io.UncheckedIOException;
import java.text.ParseException;

public class ExceptionHandlingInterceptor implements ServerInterceptor {

    @Inject
    public ExceptionHandlingInterceptor() {

    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        final var delegate = next.startCall(new SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                if (status.isOk() || status.getCause() == null) {
                    super.close(status, trailers);
                } else {
                    Throwable exception = status.getCause();
                    Status transformedStatus = mapExceptionToStatus(exception);
                    super.close(transformedStatus, trailers);
                }
            }
        }, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    call.close(mapExceptionToStatus(e), new Metadata());
                }
            }
        };
    }

    private Status mapExceptionToStatus(Throwable e) {
        if (e instanceof InvalidProtocolBufferException) {
            return Status.INTERNAL.withDescription("Internal server error").withCause(e);
        } else if (e instanceof CollectionAlreadyExistsException) {
            return Status.ALREADY_EXISTS.withDescription("Collection already exists").withCause(e);
        } else if (e instanceof UncheckedIOException) {
            return Status.INTERNAL.withDescription("IO Error occurred").withCause(e);
        } else if (e instanceof UncheckedBTreeException) {
            return Status.INTERNAL.withDescription("A Database Error occurred").withCause(e);
        } else if (e instanceof InvalidCollectionSchemaException) {
            return Status.INVALID_ARGUMENT.withDescription("Invalid Collection Schema").withCause(e);
        } else if (e instanceof CollectionDoesNotExistException) {
            return Status.NOT_FOUND.withDescription("Collection does not exist").withCause(e);
        } else if (e instanceof DocumentDoesNotExistException) {
            return Status.NOT_FOUND.withDescription("Document does not exist").withCause(e);
        } else if (e instanceof IndexNotFoundException) {
            return Status.NOT_FOUND.withDescription("Index does not exist").withCause(e);
        } else if (e instanceof PropertyDoesNotExistException) {
            return Status.NOT_FOUND.withDescription("Property does not exist").withCause(e);
        } else if (e instanceof SampleMalformedException) {
            return Status.INVALID_ARGUMENT.withDescription("Sample is malformed");
        } else if (e instanceof DocumentSchemaValidationException) {
            return Status.INVALID_ARGUMENT.withDescription("Failed to validate document's schema");
        } else if (e instanceof UnRecognizedOperatorException) {
            return Status.INVALID_ARGUMENT.withDescription("Unrecognized operator");
        } else if (e instanceof InvalidOperatorUsage) {
            return Status.INVALID_ARGUMENT.withDescription("Invalid operator usage");
        } else if (e instanceof ParseException) {
            return Status.ABORTED.withDescription("Failed to parse timestamp").withCause(e);
        } else if (e instanceof UserAlreadyExistsException) {
            return Status.ALREADY_EXISTS.withDescription("User already exists").withCause(e);
        } else if (e instanceof UserDoesNotExistException) {
            return Status.ALREADY_EXISTS.withDescription("User does not exist").withCause(e);
        } else {
            return Status.UNKNOWN.withDescription("Unknown error occurred").withCause(e);
        }
    }
}
