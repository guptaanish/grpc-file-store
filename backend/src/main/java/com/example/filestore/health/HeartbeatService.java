package com.example.filestore.health;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.filestore.interceptor.MdcKeys;

/**
 * Logs periodic heartbeat messages for application health monitoring.
 *
 * <p>Enabled by default; disable with {@code heartbeat.enabled=false}.
 * Interval is configurable via {@code heartbeat.interval} (default 300 000 ms / 5 min).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "heartbeat.enabled", havingValue = "true", matchIfMissing = true)
public class HeartbeatService {

    /**
     * Application name for MDC context.
     */
    private final String appName;

    /**
     * Heartbeat interval in milliseconds.
     */
    private final long interval;

    /**
     * Cached hostname to avoid repeated DNS lookups.
     */
    private final String hostname;

    /**
     * Creates the heartbeat service.
     *
     * @param interval interval in milliseconds between heartbeat logs.
     * @param appName  application name for MDC context.
     */
    public HeartbeatService(@Value("${heartbeat.interval:300000}") long interval,
                            @Value("${spring.application.name:grpc-file-store}") String appName) {
        this.interval = interval;
        this.appName = appName;
        this.hostname = resolveHostname();
    }

    /**
     * Logs a heartbeat message with MDC context at the configured interval.
     */
    @Scheduled(fixedRateString = "${heartbeat.interval:300000}")
    public void logHeartbeat() {
        try {
            MDC.put(MdcKeys.REQUEST_ID, "heartbeat-" + UUID.randomUUID().toString().substring(0, 8));
            MDC.put(MdcKeys.METHOD, "HEARTBEAT");
            MDC.put("hostname", hostname);
            log.info("Application heartbeat - {} is running (interval: {}ms)", appName, interval);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Resolves the local hostname.
     *
     * @return the hostname, or "unknown" if resolution fails.
     */
    private static String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
