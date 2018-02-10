package se.omfilm.gameboy;

import org.junit.Test;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HaltBugTest extends AbstractMemoryBlarggTestRoms {
    @Test(timeout = 2000)
    @ReportName("halt_bug/halt bug")
    public void itShouldHandleHaltBug() throws IOException {
        loadROM("halt_bug.zip", "halt_bug.gb");

        target.run();

        assertEquals("halt bug\n\n\nPassed\n", target.result());
    }
}
