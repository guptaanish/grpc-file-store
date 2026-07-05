package com.example.filestore.interceptor;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import io.grpc.ForwardingServerCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;

/**
 * gRPC interceptor that sets MDC context and logs call start/end with duration.
 */
@Slf4j
@Order(1)
@GrpcGlobalServerInterceptor
public class LoggingInterceptor implements ServerInterceptor {

    /**
     * Metadata key for the user-agent header.
     */
    private static final Metadata.Key<String> USER_AGENT_KEY =
            Metadata.Key.of("user-agent", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Metadata key for the request-id response header.
     */
    private static final Metadata.Key<String> REQUEST_ID_KEY =
            Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        final var clientRequestId = headers.get(REQUEST_ID_KEY);
        final var requestId = (clientRequestId != null && !clientRequestId.isBlank())
                ? clientRequestId : UUID.randomUUID().toString();
        final var method = call.getMethodDescriptor().getBareMethodName();
        final var startTime = Instant.now();

        final var remoteAddr = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        final var clientIp = remoteAddr != null ? remoteAddr.toString() : "unknown";
        final var userAgent = headers.get(USER_AGENT_KEY) != null ? headers.get(USER_AGENT_KEY) : "unknown";

        MDC.put(MdcKeys.REQUEST_ID, requestId);
        MDC.put(MdcKeys.METHOD, method);
        MDC.put(MdcKeys.CLIENT_IP, clientIp);
        MDC.put(MdcKeys.USER_AGENT, userAgent);

        final var wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(REQUEST_ID_KEY, requestId);
                super.sendHeaders(responseHeaders);
            }

            @Override
            public void close(Status status, Metadata trailers) {
                trailers.put(REQUEST_ID_KEY, requestId);
                final var duration = Duration.between(startTime, Instant.now());
                MDC.put(MdcKeys.REQUEST_ID, requestId);
                MDC.put(MdcKeys.METHOD, method);
                MDC.put(MdcKeys.CLIENT_IP, clientIp);
                MDC.put(MdcKeys.USER_AGENT, userAgent);
                MDC.put(MdcKeys.STATUS, status.getCode().name());
                MDC.put(MdcKeys.DURATION_MS, String.valueOf(duration.toMillis()));
                log.info("gRPC call completed: {} status={} duration={}ms", method, status.getCode(), duration.toMillis());
                MDC.clear();
                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }
}
