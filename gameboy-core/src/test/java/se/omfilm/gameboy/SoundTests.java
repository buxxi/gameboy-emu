package se.omfilm.gameboy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(5)
public class SoundTests extends AbstractMemoryBlarggTestRoms {
    @Test
    @ReportName("dmg_sound/01-registers")
    void itShouldHandleSoundRegisters() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/01-registers.gb");

        target.run();

        assertEquals("01-registers\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/02-len ctr")
    void itShouldHandleLengthControl() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/02-len ctr.gb");

        target.run();

        assertEquals("02-len ctr\n\n0 1 2 3 \nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/03-trigger")
    void itShouldHandleTrigger() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/03-trigger.gb");

        target.run();

        assertEquals("03-trigger\n\n0 1 2 3 \nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/04-sweep")
    void itShouldHandleSweep() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/04-sweep.gb");

        target.run();

        assertEquals("04-sweep\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/05-sweep details")
    void itShouldHandleSweepDetails() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/05-sweep details.gb");

        target.run();

        assertEquals("05-sweep details\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/06-overflow on trigger")
    void itShouldHandleOverflowOnTrigger() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/06-overflow on trigger.gb");

        target.run();

        assertEquals("06-overflow on trigger\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/07-len sweep period sync")
    void itShouldHandleLengthSweepPeriodSync() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/07-len sweep period sync.gb");

        target.run();

        assertEquals("07-len sweep period sync\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/08-len ctr during power")
    void itShouldHandleLengthControlDuringPower() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/08-len ctr during power.gb");

        target.run();

        assertEquals("08-len ctr during power\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/09-wave read while on")
    void itShouldHandleWaveReadWhileOn() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/09-wave read while on.gb");

        target.run();

        assertEquals("09-wave read while on\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/10-wave trigger while on")
    void itShouldHandleWaveTriggerWhileOn() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/10-wave trigger while on.gb");

        target.run();

        assertEquals("10-wave trigger while on\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/11-regs after power")
    void itShouldHandleRegistersAfterPower() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/11-regs after power.gb");

        target.run();

        assertEquals("11-regs after power\n\n\nPassed\n", target.result());
    }

    @Test
    @ReportName("dmg_sound/12-wave write while on")
    void itShouldHandleWaveWriteWhileOn() throws IOException {
        loadROM("dmg_sound.zip", "dmg_sound/rom_singles/12-wave write while on.gb");

        target.run();

        assertEquals("12-wave write while on\n\n\nPassed\n", target.result());
    }
}
