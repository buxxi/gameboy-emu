package se.omfilm.gameboy;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.FixedColorPalette;
import se.omfilm.gameboy.io.controller.NullController;
import se.omfilm.gameboy.io.screen.NullScreen;
import se.omfilm.gameboy.io.serial.SerialConnection;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Depends on having the test roms from Blargg. Won't run otherwise.
 * It's kinda slow since it runs a normal speed and always boots with the normal boot screen.
 */
public class BlarggTestRoms {
    private Gameboy target;
    private StringSerialConnection serial;

    @Test
    public void itShouldHandleSpecialInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/01-special.gb");
        serial.setExpected("01-special\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleInterruptInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/02-interrupts.gb");
        serial.setExpected("02-interrupts\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleSPHLInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/03-op sp,hl.gb");
        serial.setExpected("03-op sp,hl\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleRIMMInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/04-op r,imm.gb");
        serial.setExpected("04-op r,imm\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleRPInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/05-op rp.gb");
        serial.setExpected("05-op rp\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleLDRRInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/06-ld r,r.gb");
        serial.setExpected("06-ld r,r\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleCallsInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/07-jr,jp,call,ret,rst.gb");
        serial.setExpected("07-jr,jp,call,ret,rst\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleMiscInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/08-misc instrs.gb");
        serial.setExpected("08-misc instrs\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleRRInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/09-op r,r.gb");
        serial.setExpected("09-op r,r\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleBitInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/10-bit ops.gb");
        serial.setExpected("10-bit ops\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleAHLInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/11-op a,(hl).gb");
        serial.setExpected("11-op a,(hl)\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldTestInstructionTimings() throws IOException, InterruptedException {
        loadROM("instr_timing.zip", "instr_timing/instr_timing.gb");
        serial.setExpected("instr_timing\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldTestMemoryReadTimings() throws IOException, InterruptedException {
        loadROM("mem_timing.zip", "mem_timing/individual/01-read_timing.gb");
        serial.setExpected("01-read_timing\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldTestMemoryWriteTimings() throws IOException, InterruptedException {
        loadROM("mem_timing.zip", "mem_timing/individual/02-write_timing.gb");
        serial.setExpected("02-write_timing\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldTestMemoryModifyTimings() throws IOException, InterruptedException {
        loadROM("mem_timing.zip", "mem_timing/individual/03-modify_timing.gb");
        serial.setExpected("03-modify_timing\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    private void loadROM(String zipName, String romName) throws IOException {
        ZipInputStream zipFile = new ZipInputStream(getClass().getClassLoader().getResourceAsStream(zipName));
        ZipEntry entry;
        do {
            entry = zipFile.getNextEntry();
            if (entry != null && romName.equals(entry.getName())) {
                loadROM(IOUtils.toByteArray(zipFile));
                return;
            }
        } while (entry != null);
        throw new IllegalArgumentException(romName + " not found in " + zipName);
    }

    private void loadROM(byte[] rom) throws IOException {
        byte[] boot = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("boot.bin"));
        serial = new StringSerialConnection();
        target = new Gameboy(new NullScreen(), FixedColorPalette.monochrome(), new NullController(), serial, ROM.load(rom), Integer.MAX_VALUE, false).withBootData(boot);
    }

    private class StringSerialConnection implements SerialConnection {
        private String expected;
        private String result = "";
        private int index = 0;

        public void setData(int data) {
            if (expected.charAt(index) != (char) data) {
                String text = escape(expected.substring(0, index));
                target.stop();
                fail("Expected char at " + index + " to be '" + escape("" + expected.charAt(index)) + "'\nwas: '" + (escape("" + (char) data)) + "'\nAll before: '" + text + "'");
            }
            result = result + ((char) data);
            index++;
            if (index == expected.length()) {
                target.stop();
            }
        }

        public void setControl(int control) {

        }

        public int getData() {
            return 0;
        }

        public int getControl() {
            return 0;
        }

        public void setExpected(String expected) {
            this.expected = expected;
        }

        private String escape(String input) {
            return input.replaceAll("\n", "\\\\n");
        }
    }

}
