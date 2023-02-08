package se.omfilm.gameboy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(2)
public class MemoryTimingTests extends AbstractMemoryBlarggTestRoms {
    @Test
    @ReportName("mem_timing/01-read_timing")
    void itShouldTestMemoryReadTimings() throws IOException {
        loadROM("mem_timing-2.zip", "mem_timing-2/rom_singles/01-read_timing.gb");

        target.run();

        assertEquals("01-read_timing\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("mem_timing/02-write_timing")
    void itShouldTestMemoryWriteTimings() throws IOException {
        loadROM("mem_timing-2.zip", "mem_timing-2/rom_singles/02-write_timing.gb");

        target.run();

        assertEquals("02-write_timing\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("mem_timing/03-modify_timing")
    void itShouldTestMemoryModifyTimings() throws IOException {
        loadROM("mem_timing-2.zip", "mem_timing-2/rom_singles/03-modify_timing.gb");

        target.run();

        assertEquals("03-modify_timing\n\n\nPassed\n", target.result());
    }
}
