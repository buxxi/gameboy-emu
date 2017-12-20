package se.omfilm.gameboy;

import org.junit.Test;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MemoryTimingTests extends AbstractSerialBlarggTestRoms {
    @Test(timeout = 2000)
    @ReportName("mem_timing/01-read_timing")
    public void itShouldTestMemoryReadTimings() throws IOException, InterruptedException {
        loadROM("mem_timing.zip", "mem_timing/individual/01-read_timing.gb");

        target.run();

        assertEquals("01-read_timing\n\n\nPassed\n", serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("mem_timing/02-write_timing")
    public void itShouldTestMemoryWriteTimings() throws IOException, InterruptedException {
        loadROM("mem_timing.zip", "mem_timing/individual/02-write_timing.gb");

        target.run();

        assertEquals("02-write_timing\n\n\nPassed\n", serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("mem_timing/03-modify_timing")
    public void itShouldTestMemoryModifyTimings() throws IOException, InterruptedException {
        loadROM("mem_timing.zip", "mem_timing/individual/03-modify_timing.gb");

        target.run();

        assertEquals("03-modify_timing\n\n\nPassed\n", serial.result);
    }
}
