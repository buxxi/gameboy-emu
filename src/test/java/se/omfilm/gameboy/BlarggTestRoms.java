package se.omfilm.gameboy;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import se.omfilm.gameboy.internal.Gameboy;
import se.omfilm.gameboy.io.controller.Controller;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.io.serial.SerialConnection;

import java.awt.*;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Depends on having the test roms from Blargg. Won't run otherwise.
 * It's kinda slow since it runs a normal speed and always boots with the normal boot screen.
 * //TODO: make this not limited to 60fps
 */
public class BlarggTestRoms {
    private Gameboy target;
    private StringSerialConnection serial;

    @Test
    public void itShouldHandleSpecialInstructions() throws IOException, InterruptedException {
        loadROM("01-special.gb");
        serial.setExpected("01-special\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleInterruptInstructions() throws IOException, InterruptedException {
        loadROM("02-interrupts.gb");
        serial.setExpected("02-interrupts\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleSPHLInstructions() throws IOException, InterruptedException {
        loadROM("03-sphl.gb");
        serial.setExpected("03-op sp,hl\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleRIMMInstructions() throws IOException, InterruptedException {
        loadROM("04-rimm.gb");
        serial.setExpected("04-op r,imm\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleRPInstructions() throws IOException, InterruptedException {
        loadROM("05-rp.gb");
        serial.setExpected("05-op rp\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleLDRRInstructions() throws IOException, InterruptedException {
        loadROM("06-ld_rr.gb");
        serial.setExpected("06-ld r,r\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleCallsInstructions() throws IOException, InterruptedException {
        loadROM("07-calls.gb");
        serial.setExpected("07-jr,jp,call,ret,rst\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleMiscInstructions() throws IOException, InterruptedException {
        loadROM("08-misc_instrs.gb");
        serial.setExpected("08-misc instrs\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleRRInstructions() throws IOException, InterruptedException {
        loadROM("09-op_rr.gb");
        serial.setExpected("09-op r,r\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleBitInstructions() throws IOException, InterruptedException {
        loadROM("10-bit_ops.gb");
        serial.setExpected("10-bit ops\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldHandleAHLInstructions() throws IOException, InterruptedException {
        loadROM("11-op_ahl.gb");
        serial.setExpected("11-op a,(hl)\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test
    public void itShouldTestInstructionTimings() throws IOException, InterruptedException {
        loadROM("instr_timing.gb");
        serial.setExpected("instr_timing\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    private void loadROM(String romName) throws IOException {
        byte[] boot = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("boot.bin"));
        byte[] rom  = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(romName));
        serial = new StringSerialConnection();
        target = new Gameboy(new NullScreen(), new NullController(), serial, boot, rom, Integer.MAX_VALUE);
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

    private class NullScreen implements Screen {
        public void turnOn() {

        }

        public void turnOff() {

        }

        public void setPixel(int x, int y, Color color) {

        }

        public void draw() {

        }

        public boolean isOn() {
            return true;
        }
    }

    private class NullController implements Controller {
        public boolean isPressed(Button button) {
            return false;
        }
    }
}
