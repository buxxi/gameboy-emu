package se.omfilm.gameboy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.omfilm.gameboy.BlarggCompabilityReport.*;

@Timeout(2)
public class OAMBugTests extends AbstractMemoryBlarggTestRoms {
    @Test
    @ReportName("oam_bug/1-lcd sync")
    void itShouldHandleLCDSync() throws IOException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/1-lcd_sync.gb");

        target.run();

        assertEquals("1-lcd sync\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("oam_bug/2-causes")
    void itShouldHandleCauses() throws IOException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/2-causes.gb");

        target.run();

        assertEquals("2-causes\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("oam_bug/3-non_causes")
    void itShouldHandleNonCauses() throws IOException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/3-non_causes.gb");

        target.run();

        assertEquals("3-non_causes\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("oam_bug/4-scanline_timing")
    void itShouldHandleScanlineTiming() throws IOException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/4-scanline_timing.gb");

        target.run();

        assertEquals("4-scanline_timing\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("oam_bug/5-timing_bug")
    void itShouldHandleTimingBug() throws IOException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/5-timing_bug.gb");

        target.run();

        assertEquals("5-timing_bug\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("oam_bug/6-timing_no_bug")
    void itShouldHandleNoTimingBug() throws IOException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/6-timing_no_bug.gb");

        target.run();

        assertEquals("6-timing_no_bug\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("oam_bug/7-timing_effect")
    void itShouldHandleTimingEffect() throws IOException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/7-timing_effect.gb");

        target.run();

        assertEquals("7-timing_effect\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("oam_bug/8-instr_effect")
    void itShouldHandleInstructionEffect() throws IOException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/8-instr_effect.gb");

        target.run();

        assertEquals("8-instr_effect\n\n\nPassed\n", target.result());
    }   
}
