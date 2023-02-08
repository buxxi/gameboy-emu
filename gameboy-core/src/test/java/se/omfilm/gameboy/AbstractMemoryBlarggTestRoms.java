package se.omfilm.gameboy;

import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.FixedColorPalette;
import se.omfilm.gameboy.io.controller.NullController;
import se.omfilm.gameboy.io.screen.NullScreen;
import se.omfilm.gameboy.io.serial.NullSerialConnection;
import se.omfilm.gameboy.io.sound.NullSoundPlayback;

import java.io.ByteArrayOutputStream;
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
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                zipFile.transferTo(out);
                loadROM(out.toByteArray());
                return;
            }
        } while (entry != null);
        throw new IllegalArgumentException(romName + " not found in " + zipName);
    }

    private void loadROM(byte[] rom) {
        target = new BlarggGameboy(rom);
        target.reset();
    }

    public static class BlarggGameboy extends Gameboy {
        public BlarggGameboy(byte[] rom) {
            super(new NullScreen(), FixedColorPalette.PRESET.MONOCHROME.getPalette(), new NullController(), new NullSerialConnection(), new NullSoundPlayback(), ROM.load(rom), Speed.UNLIMITED);
        }

        @Override
        public void run() {
            int prev = 0;
            while (!Thread.currentThread().isInterrupted()) {
                step();
                //Verify that the tests even has begun to run
                if (mmu.readByte(0xA001) == 0xDE && mmu.readByte(0xA002) == 0xB0 && mmu.readByte(0xA003) == 0x61) {

                    if (prev != mmu.readByte(0xA000)) {
                        //If the previous value was 0x80 (that the test was running) it should now be done
                        if (prev == 0x80) {
                            break;
                        }
                        prev = mmu.readByte(0xA000);
                    }
                }
            }
        }

        public String result() {
            StringBuilder result = new StringBuilder();
            int pos = 0xA004;
            while (mmu.readByte(pos) != 0) {
                result.append((char) mmu.readByte(pos++));
            }
            return result.toString();
        }
    }
}
