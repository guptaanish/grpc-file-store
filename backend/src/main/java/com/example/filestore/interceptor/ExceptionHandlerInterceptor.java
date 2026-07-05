package com.example.filestore.interceptor;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.core.annotation.Order;

import com.example.filestore.service.FileNotFoundException;
import com.example.filestore.service.StorageQuotaExceededException;

/**
 * gRPC interceptor that catches uncaught exceptions and maps them to gRPC Status codes.
 */
@Slf4j
@Order(4)
@GrpcGlobalServerInterceptor
public class ExceptionHandlerInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        final var listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    handleException(call, e);
                }
            }

            @Override
            public void onMessage(ReqT message) {
                try {
                    super.onMessage(message);
                } catch (Exception e) {
                    handleException(call, e);
                }
            }
        };
    }

    /**
     * Maps exceptions to appropriate gRPC status codes.
     *
     * @param call the server call.
     * @param e    the exception to handle.
     * @param <ReqT>  request type.
     * @param <RespT> response type.
     */
    private <ReqT, RespT> void handleException(ServerCall<ReqT, RespT> call, Exception e) {
        log.error("Unhandled exception in gRPC call: {}", e.getMessage(), e);
        final var status = switch (e) {
            case FileNotFoundException ex -> Status.NOT_FOUND.withDescription(ex.getMessage());
            case StorageQuotaExceededException ex -> Status.RESOURCE_EXHAUSTED.withDescription(ex.getMessage());
            case IllegalArgumentException ex -> Status.INVALID_ARGUMENT.withDescription(ex.getMessage());
            case java.io.UncheckedIOException ex -> Status.INTERNAL.withDescription("Storage I/O error");
            default -> Status.INTERNAL.withDescription("Internal server error");
        };
        call.close(status, new Metadata());
    }
}
