package se.omfilm.gameboy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.util.DebugPrinter;

public class ROM extends ByteArrayMemory {
    private static final Logger log = LoggerFactory.getLogger(ROM.class);

    public ROM(byte[] data) {
        super(verifyRom(data));
    }

    private static byte[] verifyRom(byte[] rom) {
        log.info("Game:\t\t" + readGameName(rom));
        if (rom[0x146] != 0) {
            throw new IllegalArgumentException("Can only handle the original GameBoy");
        }
        if (rom[0x147] != 0) {
            throw new IllegalArgumentException("Can only handle Cartridge Types of ROM Only");
        }
        ROM_SIZE romSize = ROM_SIZE.values()[rom[0x148]];
        if (romSize.expectedSize != rom.length) {
            throw new IllegalArgumentException("Roms actual size doesn't match the expected " + romSize.expectedSize + "!=" + rom.length);
        }
        log.info("ROM Size:\t" + romSize + " (" + DebugPrinter.hex(romSize.expectedSize, 4) + ")");

        RAM_SIZE ramSize = RAM_SIZE.values()[rom[0x149]];
        log.info("RAM Size:\t" + ramSize + " (" + DebugPrinter.hex(ramSize.expectedSize, 4) + ")");
        if (ramSize != RAM_SIZE._2KB) {
            throw new IllegalArgumentException("Can only handle RAM Size " + RAM_SIZE._2KB);
        }

        log.info("Region:\t\t" + (rom[0x14A] == 0 ? "Japan" : "International"));
        log.info("C-check:\t" + (rom[0x14D]));
        log.info("Checksum:\t" + DebugPrinter.hex((rom[0x14E] << 8) + rom[0x14F], 4));
        return rom;
    }

    private static String readGameName(byte[] rom) {
        byte[] name = new byte[16];
        System.arraycopy(rom, 0x0134, name, 0, 16);
        return new String(name);
    }

    private enum ROM_SIZE {
        _32KB(32 * 1024),
        _64KB(64 * 1024),
        _128KB(128 * 1024),
        _256KB(256 * 1024),
        _512KB(512 * 1024),
        _1MB(1024 * 1024),
        _2MB(2048 * 1024);

        private final int expectedSize;

        ROM_SIZE(int expectedSize) {
            this.expectedSize = expectedSize;
        }

        public String toString() {
            return super.toString().substring(1);
        }
    }

    private enum RAM_SIZE {
        _2KB(2 * 1024, 1),
        _8KB(8 * 1024, 1),
        _32KB(32 * 1024, 4),
        _128KB(128 * 1024, 16);

        private final int expectedSize;
        private final int banks;

        RAM_SIZE(int expectedSize, int banks) {
            this.expectedSize = expectedSize;
            this.banks = banks;
        }

        public String toString() {
            return super.toString().substring(1);
        }
    }
}
