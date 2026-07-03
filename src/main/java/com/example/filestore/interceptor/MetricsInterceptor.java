package com.example.filestore.interceptor;

import java.util.Objects;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.core.annotation.Order;

/**
 * gRPC interceptor that records Micrometer metrics for each RPC call.
 */
@Order(3)
@GrpcGlobalServerInterceptor
public class MetricsInterceptor implements ServerInterceptor {

    /**
     * Micrometer meter registry for recording metrics.
     */
    private final MeterRegistry meterRegistry;

    /**
     * Constructs the metrics interceptor.
     *
     * @param meterRegistry the meter registry.
     */
    public MetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry must not be null");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        final var method = call.getMethodDescriptor().getBareMethodName();
        final var timerSample = Timer.start(meterRegistry);

        final var wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                timerSample.stop(Timer.builder("grpc.server.requests.duration")
                        .tag("method", method != null ? method : "unknown")
                        .tag("status", status.getCode().name())
                        .register(meterRegistry));

                meterRegistry.counter("grpc.server.requests.count",
                        "method", method != null ? method : "unknown",
                        "status", status.getCode().name()
                ).increment();

                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }
}
