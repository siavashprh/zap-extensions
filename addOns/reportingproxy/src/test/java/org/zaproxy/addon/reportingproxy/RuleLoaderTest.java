package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuleLoaderTest {

    @TempDir
    Path tempDir;

    private RuleLoader ruleLoader;

    @BeforeEach
    void setUp() {
        ruleLoader = new RuleLoader();
    }

    @Test
    void shouldThrowExceptionIfFileDoesNotExist() {
        File nonExistentFile = tempDir.resolve("does-not-exist.jar").toFile();
        assertThrows(IOException.class, () -> ruleLoader.loadRules(nonExistentFile));
    }

    @Test
    void shouldThrowExceptionIfFileIsNotJar() throws Exception {
        File textFile = tempDir.resolve("test.txt").toFile();
        assertTrue(textFile.createNewFile());
        assertThrows(IOException.class, () -> ruleLoader.loadRules(textFile));
    }

    @Test
    @SuppressWarnings("try")
    void shouldReturnEmptyListForEmptyJar() throws Exception {
        // Create a valid but empty JAR
        File emptyJar = tempDir.resolve("empty.jar").toFile();
        try (java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(new java.io.FileOutputStream(emptyJar))) {
            // No entries, just creating an empty JAR
            jos.flush();
        }
        
        java.util.List<ReportingRule> rules = ruleLoader.loadRules(emptyJar);
        assertTrue(rules.isEmpty());
    }
}
