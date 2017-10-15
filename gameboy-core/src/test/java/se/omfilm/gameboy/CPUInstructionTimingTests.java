package se.omfilm.gameboy;

import org.junit.Test;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CPUInstructionTimingTests extends AbstractSerialBlarggTestRoms {
    @Test(timeout = 2000)
    @ReportName("instr_timing/instr_timing")
    public void itShouldTestInstructionTimings() throws IOException, InterruptedException {
        loadROM("instr_timing.zip", "instr_timing/instr_timing.gb");
        serial.setExpected("instr_timing\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }
}
