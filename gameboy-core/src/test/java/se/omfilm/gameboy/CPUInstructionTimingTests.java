package se.omfilm.gameboy;

import org.junit.Test;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CPUInstructionTimingTests extends AbstractSerialBlarggTestRoms {
    @Test(timeout = 2000)
    @ReportName("instr_timing/instr_timing")
    public void itShouldTestInstructionTimings() throws IOException {
        loadROM("instr_timing.zip", "instr_timing/instr_timing.gb");

        target.run();

        assertEquals("instr_timing\n\n\nPassed\n", serial.result);
    }
}
