package se.omfilm.gameboy;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlarggCompabilityReport {
    public static void main(String[] args) throws FileNotFoundException {
        File reportFile = resolveFile();
        Map<String, Boolean> success = collectResult();

        List<String> strings = new ArrayList<>(success.keySet());
        strings.sort(String::compareTo);

        String group = "";

        try (PrintStream out = new PrintStream(new FileOutputStream(reportFile))) {
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

    private static File resolveFile() {
        Path path = Paths.get("").toAbsolutePath();
        do {
            File file = path.resolve("COMPABILITY.md").toFile();
            if (file.exists()) {
                return file;
            }
            path = path.getParent();
        } while (path != null);
        throw new RuntimeException("No COMPABILITY.md found");
    }

    private static Map<String, Boolean> collectResult() {
        Map<String, Boolean> success = new HashMap<>();

        JUnitCore runner = new JUnitCore();
        runner.addListener(new RunListener() {
            @Override
            public void testFinished(Description description) {
                String name = description.getAnnotation(ReportName.class).value();
                if (!success.containsKey(name)) {
                    success.put(name, true);
                }
            }

            @Override
            public void testFailure(Failure failure) {
                success.put(failure.getDescription().getAnnotation(ReportName.class).value(), false);
            }
        });

        runner.run(
                CPUInstructionTests.class,
                CPUInstructionTimingTests.class,
                HaltBugTest.class,
                InterruptTimeTest.class,
                MemoryTimingTests.class,
                SoundTests.class,
                OAMBugTests.class
        );
        return success;
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
