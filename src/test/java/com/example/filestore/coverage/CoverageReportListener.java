package com.example.filestore.coverage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * Writes the test-to-production coverage mapping report after all tests finish.
 */
public class CoverageReportListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        final var coverageMap = CoverageTrackingExtension.getCoverageMap();
        if (coverageMap.isEmpty()) {
            return;
        }

        try {
            final var outputDir = Path.of("build", "reports");
            Files.createDirectories(outputDir);

            final var mapper = new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

            // Test class -> Classes map (merged across methods)
            final var mergedMap = new java.util.TreeMap<String, java.util.TreeSet<String>>();
            for (var entry : coverageMap.entrySet()) {
                final var testClass = entry.getKey().substring(0, entry.getKey().indexOf('#'));
                mergedMap.computeIfAbsent(testClass, k -> new java.util.TreeSet<>()).addAll(entry.getValue());
            }
            mapper.writeValue(outputDir.resolve("test-exercises-production-classes.json").toFile(), mergedMap);

            // Reverse map: Class -> Test classes
            final var reverseMap = new java.util.TreeMap<String, java.util.TreeSet<String>>();
            for (var entry : coverageMap.entrySet()) {
                final var testClass = entry.getKey().substring(0, entry.getKey().indexOf('#'));
                for (var className : entry.getValue()) {
                    reverseMap.computeIfAbsent(className, k -> new java.util.TreeSet<>()).add(testClass);
                }
            }
            mapper.writeValue(outputDir.resolve("production-class-tested-by.json").toFile(), reverseMap);

            System.out.println("[CoverageReport] Written to: " + outputDir.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[CoverageReport] Failed to write report: " + e.getMessage());
        }
    }
}
