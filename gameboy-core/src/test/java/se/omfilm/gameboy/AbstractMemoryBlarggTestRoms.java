package se.omfilm.gameboy;

import org.apache.commons.io.IOUtils;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.FixedColorPalette;
import se.omfilm.gameboy.io.controller.NullController;
import se.omfilm.gameboy.io.screen.NullScreen;
import se.omfilm.gameboy.io.serial.NullSerialConnection;
import se.omfilm.gameboy.io.sound.NullSoundPlayback;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AbstractMemoryBlarggTestRoms {
    protected BlarggGameboy target;

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
        target = new BlarggGameboy(rom);
        target.reset();
    }

    public class BlarggGameboy extends Gameboy {
        public BlarggGameboy(byte[] rom) throws IOException {
            super(new NullScreen(), FixedColorPalette.PRESET.MONOCHROME.getPalette(), new NullController(), new NullSerialConnection(), new NullSoundPlayback(), ROM.load(rom), Speed.UNLIMITED, false);
        }

        @Override
        public void run() throws InterruptedException {
            int prev = 0;
            while (true) {
                step();
                //Verify that the tests even has begun to run
                if (memory.readByte(0xA001) == 0xDE && memory.readByte(0xA002) == 0xB0 && memory.readByte(0xA003) == 0x61) {

                    if (prev != memory.readByte(0xA000)) {
                        //If the previous value was 0x80 (that the test was running) it should now be done
                        if (prev == 0x80) {
                            break;
                        }
                        prev = memory.readByte(0xA000);
                    }
                }
            }
        }

        public String result() {
            StringBuilder result = new StringBuilder();
            int pos = 0xA004;
            while (memory.readByte(pos) != 0) {
                result.append((char) memory.readByte(pos++));
            }
            return result.toString();
        }
    }
}
