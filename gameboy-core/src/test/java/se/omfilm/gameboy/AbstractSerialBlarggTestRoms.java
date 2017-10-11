package se.omfilm.gameboy;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.FixedColorPalette;
import se.omfilm.gameboy.io.controller.NullController;
import se.omfilm.gameboy.io.screen.NullScreen;
import se.omfilm.gameboy.io.serial.SerialConnection;
import se.omfilm.gameboy.io.sound.NullSoundPlayback;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Depends on having the test roms from Blargg. Won't run otherwise.
 * Runs at maximum possible speed and skips the regular boot screen.
 * Can only use the tests that outputs to the serial connection (v2 of the tests seems to not have that)
 */
public abstract class AbstractSerialBlarggTestRoms {
    protected StringSerialConnection serial;
    protected Gameboy target;

    protected void loadROM(String zipName, String romName) throws IOException {
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
        serial = new StringSerialConnection();
        target = new Gameboy(new NullScreen(), FixedColorPalette.PRESET.MONOCHROME.getPalette(), new NullController(), serial, new NullSoundPlayback(), ROM.load(rom), Gameboy.Speed.UNLIMITED, false);
        target.reset();
    }

    protected class StringSerialConnection implements SerialConnection {
        protected String expected;
        protected String result = "";
        private int index = 0;

        public void data(int data) {
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

        public void control(int control) {

        }

        public int data() {
            return 0;
        }

        public int control() {
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
