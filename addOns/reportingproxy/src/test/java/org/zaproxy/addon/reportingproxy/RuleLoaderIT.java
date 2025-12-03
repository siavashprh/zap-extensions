package org.zaproxy.addon.reportingproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RuleLoaderIT {

    @TempDir
    Path tempDir;

    private RuleLoader ruleLoader;
    private File jarFile;

    @BeforeEach
    void setUp() throws Exception {
        ruleLoader = new RuleLoader();
        jarFile = tempDir.resolve("test-rules.jar").toFile();
        createTestJar(jarFile);
    }

    @Test
    void shouldLoadRuleFromJar() throws Exception {
        List<ReportingRule> rules = ruleLoader.loadRules(jarFile);
        
        assertNotNull(rules);
        assertEquals(1, rules.size());
        ReportingRule rule = rules.get(0);
        assertEquals("Generated Rule", rule.getName());
        assertEquals("Generated", rule.getDescription());
    }

    private void createTestJar(File jarFile) throws Exception {
        String source = "package org.zaproxy.addon.reportingproxy.generated;\n" +
                "import org.zaproxy.addon.reportingproxy.ReportingRule;\n" +
                "import org.parosproxy.paros.network.HttpMessage;\n" +
                "public class GeneratedRule implements ReportingRule {\n" +
                "    @Override public void scan(HttpMessage msg) {}\n" +
                "    @Override public String getName() { return \"Generated Rule\"; }\n" +
                "    @Override public String getDescription() { return \"Generated\"; }\n" +
                "}";

        File sourceFile = tempDir.resolve("GeneratedRule.java").toFile();
        Files.write(sourceFile.toPath(), source.getBytes());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Cannot find system Java compiler. Ensure you are running with a JDK.");
        }
        
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        List<String> options = Arrays.asList("-classpath", System.getProperty("java.class.path"));
        
        JavaCompiler.CompilationTask task = compiler.getTask(
                null, 
                fileManager, 
                null, 
                options, 
                null, 
                fileManager.getJavaFileObjects(sourceFile));
        
        Boolean result = task.call();
        if (result == null || !result) {
            throw new RuntimeException("Compilation failed");
        }

        File classFile = tempDir.resolve("GeneratedRule.class").toFile();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            JarEntry entry = new JarEntry("org/zaproxy/addon/reportingproxy/generated/GeneratedRule.class");
            jos.putNextEntry(entry);
            Files.copy(classFile.toPath(), jos);
            jos.closeEntry();
        }
    }
}
