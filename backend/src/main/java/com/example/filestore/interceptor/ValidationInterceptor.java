package com.example.filestore.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.core.annotation.Order;

/**
 * gRPC interceptor for request field validation.
 * Validates common fields across all RPCs. Specific validation is handled in the service layer.
 */
@Order(2)
@GrpcGlobalServerInterceptor
public class ValidationInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        // Field-level validation is handled in service methods for gRPC streaming compatibility.
        // This interceptor serves as a hook point for cross-cutting request validation (e.g., auth headers).
        return next.startCall(call, headers);
    }
}
