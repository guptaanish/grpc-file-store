package com.example.filestore.coverage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that captures per-test coverage via the JaCoCo agent's JMX interface.
 * Maps each test method to the production classes it exercises.
 */
public class CoverageTrackingExtension implements BeforeEachCallback, AfterEachCallback {

    /**
     * Coverage mapping: test name -> list of production classes exercised.
     */
    private static final Map<String, List<String>> COVERAGE_MAP = new ConcurrentHashMap<>();

    /**
     * JaCoCo agent MBean name.
     */
    private static final String JACOCO_MBEAN = "org.jacoco:type=Runtime";

    /**
     * Returns the collected coverage map.
     *
     * @return map of test name to covered class names.
     */
    public static Map<String, List<String>> getCoverageMap() {
        return Map.copyOf(COVERAGE_MAP);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (!isEnabled()) {
            return;
        }
        final var mbean = getJacocoMBean();
        if (mbean != null) {
            mbean.invoke(new ObjectName(JACOCO_MBEAN), "reset", new Object[]{}, new String[]{});
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (!isEnabled()) {
            return;
        }
        final var mbean = getJacocoMBean();
        if (mbean == null) {
            return;
        }

        final var testName = context.getRequiredTestClass().getName()
                + "#" + context.getRequiredTestMethod().getName();

        final var data = (byte[]) mbean.invoke(
                new ObjectName(JACOCO_MBEAN),
                "getExecutionData",
                new Object[]{false},
                new String[]{"boolean"});

        final var executionDataStore = new ExecutionDataStore();
        final var sessionInfoStore = new SessionInfoStore();
        try (var input = new ByteArrayInputStream(data)) {
            final var reader = new ExecutionDataReader(input);
            reader.setExecutionDataVisitor(executionDataStore);
            reader.setSessionInfoVisitor(sessionInfoStore);
            reader.read();
        }

        final var classesDir = new File("build/classes/java/main");
        if (!classesDir.exists()) {
            return;
        }

        final var coverageBuilder = new CoverageBuilder();
        final var analyzer = new Analyzer(executionDataStore, coverageBuilder);
        analyzer.analyzeAll(classesDir);

        final List<String> coveredClasses = new ArrayList<>();
        for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {
            final var className = classCoverage.getName().replace('/', '.');
            if (classCoverage.getMethodCounter().getCoveredCount() > 0
                    && !className.contains("$")
                    && !isGeneratedClass(className)) {
                coveredClasses.add(className);
            }
        }

        if (!coveredClasses.isEmpty()) {
            coveredClasses.sort(String::compareTo);
            COVERAGE_MAP.put(testName, coveredClasses);
        }
    }

    /**
     * Checks if coverage tracking is enabled via system property.
     *
     * @return true if enabled.
     */
    private static boolean isEnabled() {
        return Boolean.getBoolean("coverage.tracking.enabled");
    }

    /**
     * Checks if a class is generated code (protobuf, gRPC stubs).
     *
     * @param className fully qualified class name.
     * @return true if the class is generated.
     */
    private static boolean isGeneratedClass(String className) {
        if (className.contains("$Builder") || className.contains("Grpc$")) {
            return true;
        }
        if (className.endsWith("Grpc") || className.matches(".*Grpc\\$\\d+")) {
            return true;
        }
        final var simpleName = className.substring(className.lastIndexOf('.') + 1);
        if (className.startsWith("com.example.filestore.grpc.")
                && !simpleName.equals("FileStoreGrpcService")
                && !simpleName.equals("HealthCheckService")) {
            return true;
        }
        return false;
    }

    /**
     * Gets the MBeanServer if the JaCoCo agent MBean is registered.
     *
     * @return the MBeanServer, or null if JaCoCo agent is not available.
     */
    private MBeanServer getJacocoMBean() {
        try {
            final var mbs = ManagementFactory.getPlatformMBeanServer();
            final var name = new ObjectName(JACOCO_MBEAN);
            if (mbs.isRegistered(name)) {
                return mbs;
            }
        } catch (Exception e) {
            // JaCoCo agent not available
        }
        return null;
    }
}
