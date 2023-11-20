package smoketest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EnsureDatadogAgentCanBeLoadedTest {
    public static Stream<Arguments> ddAgents() {
        return Stream.of(
                Arguments.of(System.getProperty("datadog.agent.1.23.0.path"), true),
                Arguments.of(System.getProperty("datadog.agent.1.24.0.path"), false)
        );
    }

    @ParameterizedTest
    @MethodSource("ddAgents")
    public void ddAgents_supporting_multiple_agents_without_code_source(String ddPath, boolean expectFailure) throws IOException, InterruptedException {
        // Given
        var processBuilder = new ProcessBuilder(List.of(
                "java",
                "-javaagent:" + ddPath,
                "-Djava.system.class.loader=smoketest.classloader.NoProtectionDomainClassLoader",
                "-javaagent:" + System.getProperty("smoketest.another.javaagent.path"),
                "-jar", System.getProperty("smoketest.jar.path")
        ));
        processBuilder.directory(new File(System.getProperty("buildDir")));
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("JAVA_HOME", System.getProperty("java.home"));
        processBuilder.environment().put("DD_API_KEY", "01234567890abcdef123456789ABCDEF");

        // When
        var process = processBuilder.start();
        int exitCode = process.waitFor();

        // Then
        var outAndErr = new String(process.getInputStream().readAllBytes()); // straw man capture, proper impl should use thread
        assertAll(
                "Asserting stdout and stderr \n=============================\n" + outAndErr + "\n=============================\n",
                () -> assertEquals(0, exitCode),
                () -> assertEquals(expectFailure, outAndErr.contains("ERROR"), "No ERROR in output"),
                () -> assertEquals(expectFailure, outAndErr.contains("java.lang.IllegalStateException: Multiple javaagents specified and code source unavailable"), "No multiple javaagents error in output")
        );
    }
}
