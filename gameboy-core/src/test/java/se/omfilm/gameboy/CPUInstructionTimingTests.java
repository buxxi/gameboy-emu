package se.omfilm.gameboy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(2)
public class CPUInstructionTimingTests extends AbstractSerialBlarggTestRoms {
    @Test
    @ReportName("instr_timing/instr_timing")
    public void itShouldTestInstructionTimings() throws IOException {
        loadROM("instr_timing.zip", "instr_timing/instr_timing.gb");

        target.run();

        assertEquals("instr_timing\n\n\nPassed\n", serial.result);
    }
}
