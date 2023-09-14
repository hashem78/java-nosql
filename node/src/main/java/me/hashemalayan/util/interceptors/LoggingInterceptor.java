package me.hashemalayan.util.interceptors;

import com.google.inject.Inject;
import io.grpc.*;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import org.slf4j.Logger;


public class LoggingInterceptor implements ServerInterceptor {

    private final Logger logger;

    @Inject
    public LoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {

        final var delegate = next.startCall(new SimpleForwardingServerCall<>(call) {
            @Override
            public void sendMessage(RespT message) {
                super.sendMessage(message);
            }

            @Override
            public void close(Status status, Metadata trailers) {
                if (!status.isOk()) {
                    logger.warn(
                            "Call finished with status: "
                                    + status.getCode()
                                    + " " + status.getDescription()
                    );
                }
                super.close(status, trailers);
            }
        }, headers);

        return new SimpleForwardingServerCallListener<>(delegate) {
            @Override
            public void onMessage(ReqT message) {
                logger.info("Request on method: " + call.getMethodDescriptor().getFullMethodName());
                super.onMessage(message);
            }
        };
    }
}
