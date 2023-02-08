package se.omfilm.gameboy;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class BlarggCompabilityReport {
    public static void main(String[] args) throws IOException {
        Path reportFile = resolveFile();
        Map<String, Boolean> success = collectResult();

        List<String> strings = new ArrayList<>(success.keySet());
        strings.sort(String::compareTo);

        String group = "";

        try (PrintStream out = new PrintStream(Files.newOutputStream(reportFile))) {
            out.println("### Blargg Test ROMS result");
            for (String test : strings) {
                String currentGroup = test.substring(0, test.indexOf('/'));
                String currentName = test.substring(test.indexOf('/') + 1);
                if (!group.equals(currentGroup)) {
                    boolean allOk = checkAllOk(success, currentGroup);
                    out.println(" - [" + (allOk ? 'x' : ' ') + "] " + currentGroup + ":");
                }
                out.println("\t- [" + (success.get(test) ? 'x' : ' ') + "] " + currentName);
                group = currentGroup;
            }
        }
    }

    private static Path resolveFile() {
        Path path = Paths.get("").toAbsolutePath();
        do {
            Path file = path.resolve("COMPABILITY.md");
            if (Files.exists(file)) {
                return file;
            }
            path = path.getParent();
        } while (path != null);
        throw new RuntimeException("No COMPABILITY.md found");
    }

    private static Map<String, Boolean> collectResult() {
        Map<String, Boolean> success = new HashMap<>();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectClass(CPUInstructionTests.class),
                        selectClass(CPUInstructionTimingTests.class),
                        selectClass(HaltBugTest.class),
                        selectClass(InterruptTimeTest.class),
                        selectClass(MemoryTimingTests.class),
                        selectClass(SoundTests.class),
                        selectClass(OAMBugTests.class))
                .build();

        Launcher launcher = LauncherFactory.create();

        launcher.registerTestExecutionListeners(new TestExecutionListener() {
            public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                resolveReportName(testIdentifier).ifPresent(reportName -> success.put(reportName, testExecutionResult.getStatus() == SUCCESSFUL));
            }
        });
        launcher.execute(request);

        return success;
    }

    private static Optional<String> resolveReportName(TestIdentifier testIdentifier) {
        return testIdentifier.getSource()
                .filter(s -> s instanceof MethodSource)
                .map(s -> (MethodSource) s)
                .map(MethodSource::getJavaMethod)
                .map(method -> method.getAnnotation(ReportName.class))
                .map(ReportName::value);
    }

    private static boolean checkAllOk(Map<String, Boolean> success, String currentGroup) {
        return success.entrySet().stream().allMatch(e -> !e.getKey().startsWith(currentGroup) || e.getValue());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface ReportName {
        String value();
    }
}
