package se.omfilm.gameboy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(2)
public class HaltBugTest extends AbstractMemoryBlarggTestRoms {
    @Test
    @ReportName("halt_bug/halt bug")
    public void itShouldHandleHaltBug() throws IOException {
        loadROM("halt_bug.zip", "halt_bug.gb");

        target.run();

        assertEquals("halt bug\n\n\nPassed\n", target.result());
    }
}
