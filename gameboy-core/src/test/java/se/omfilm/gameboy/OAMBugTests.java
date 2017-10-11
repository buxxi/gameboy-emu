package se.omfilm.gameboy;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class OAMBugTests extends AbstractMemoryBlarggTestRoms {
    @Test(timeout = 2000)
    public void itShouldHandleLCDSync() throws IOException, InterruptedException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/1-lcd_sync.gb");

        target.run();

        assertEquals("1-lcd sync\n\n\nPassed\n", target.result());
    }

    @Test(timeout = 2000)
    public void itShouldHandleCauses() throws IOException, InterruptedException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/2-causes.gb");

        target.run();

        assertEquals("2-causes\n\n\nPassed\n", target.result());
    }

    @Test(timeout = 2000)
    public void itShouldHandleNonCauses() throws IOException, InterruptedException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/3-non_causes.gb");

        target.run();

        assertEquals("3-non_causes\n\n\nPassed\n", target.result());
    }

    @Test(timeout = 2000)
    public void itShouldHandleScanlineTiming() throws IOException, InterruptedException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/4-scanline_timing.gb");

        target.run();

        assertEquals("4-scanline_timing\n\n\nPassed\n", target.result());
    }

    @Test(timeout = 2000)
    public void itShouldHandleTimingBug() throws IOException, InterruptedException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/5-timing_bug.gb");

        target.run();

        assertEquals("5-timing_bug\n\n\nPassed\n", target.result());
    }

    @Test(timeout = 2000)
    public void itShouldHandleNoTimingBug() throws IOException, InterruptedException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/6-timing_no_bug.gb");

        target.run();

        assertEquals("6-timing_no_bug\n\n\nPassed\n", target.result());
    }

    @Test(timeout = 2000)
    public void itShouldHandleTimingEffect() throws IOException, InterruptedException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/7-timing_effect.gb");

        target.run();

        assertEquals("7-timing_effect\n\n\nPassed\n", target.result());
    }

    @Test(timeout = 2000)
    public void itShouldHandleInstructionEffect() throws IOException, InterruptedException {
        loadROM("oam_bug.zip", "oam_bug/rom_singles/8-instr_effect.gb");

        target.run();

        assertEquals("8-instr_effect\n\n\nPassed\n", target.result());
    }   
}
