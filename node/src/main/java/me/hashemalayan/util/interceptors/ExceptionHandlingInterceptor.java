package me.hashemalayan.util.interceptors;

import btree4j.BTreeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.*;
import me.hashemalayan.services.db.exceptions.*;

import java.io.IOException;

public class ExceptionHandlingInterceptor implements ServerInterceptor {

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
                    Throwable exception = status.getCause().getCause();
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
                    call.close(mapExceptionToStatus(e.getCause()), new Metadata());
                }
            }
        };
    }

    private Status mapExceptionToStatus(Throwable e) {
        if (e instanceof InvalidProtocolBufferException) {
            return Status.INTERNAL.withDescription("Internal server error").withCause(e);
        } else if (e instanceof CollectionAlreadyExistsException) {
            return Status.ALREADY_EXISTS.withDescription("Collection already exists").withCause(e);
        } else if (e instanceof IOException) {
            if (e instanceof JsonProcessingException)
                return Status.INTERNAL.withDescription("Json Processing Error occurred").withCause(e);
            return Status.INTERNAL.withDescription("IO Error occurred").withCause(e);
        } else if (e instanceof BTreeException) {
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
        } else if (e instanceof AffineNodeIsDownException) {
            return Status.UNAVAILABLE.withDescription("Affine node for the document is down");
        } else {
            e.printStackTrace();
            return Status.UNKNOWN.withDescription("Unknown error occurred").withCause(e);
        }
    }
}
