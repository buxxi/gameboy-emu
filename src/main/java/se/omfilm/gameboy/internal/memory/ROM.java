package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.util.DebugPrinter;

public class ROM {
    private static final Logger log = LoggerFactory.getLogger(ROM.class);

    private final byte[] data;
    private final ROMType romType;
    private final ROMSize romSize;
    private final RAMSize ramSize;
    private final String name;
    private final Model model;
    private final Region region;

    private ROM(byte[] data) {
        this.data = data;
        this.romType = ROMType.fromValue(data[0x147]);
        this.romSize = ROMSize.values()[data[0x148]];
        this.ramSize = RAMSize.values()[data[0x149]];
        this.name = readGameName(data);
        this.model = Model.fromValue(data[0x143] & 0xFF);
        this.region = Region.fromValue(data[0x14A]);
    }

    public static ROM load(byte[] data) {
        ROM rom = new ROM(data);
        if (data[0x146] != 0) {
            throw new IllegalArgumentException("Can only handle the original GameBoy");
        }
        if (rom.romType.value > 3) {
            throw new IllegalArgumentException("Can't handle rom of type " + rom.romType);
        }

        if (rom.romSize.expectedSize != data.length) {
            throw new IllegalArgumentException("Roms actual size doesn't match the expected " + rom.romSize.expectedSize + "!=" + data.length);
        }
        return rom;
    }

    public BankableRAM createRAMBanks() {
        return new BankableRAM(ramSize.banks, Memory.MemoryType.RAM_BANKS.size());
    }

    public Memory createROMBanks(BankableRAM ramBanks) {
        ByteArrayMemory rom = new ByteArrayMemory(data);
        switch (romType) {
            case ROM_ONLY:
                return new ReadOnlyMemory(rom);
            case ROM_MBC1:
            case ROM_MBC1_RAM:
            case ROM_MBC1_RAM_BATTERY:
                return new MBC1(rom, ramBanks);
            default:
                throw new IllegalArgumentException("Can't create ROM from type: " + romType);
        }
    }

    public void print() {
        log.info("Game: " + name);
        log.info("Model: " + model);
        log.info("ROM Type: " + romType);
        log.info("ROM Size: " + romSize + " (" + DebugPrinter.hex(romSize.expectedSize, 4) + ")");
        log.info("RAM Size: " + ramSize + " (" + DebugPrinter.hex(ramSize.expectedSize, 4) + ")");
        log.info("Region: " + region);
    }

    private static String readGameName(byte[] rom) {
        byte[] name = new byte[16];
        System.arraycopy(rom, 0x0134, name, 0, 16);
        return new String(name);
    }

    private enum Model {
        GAMEBOY,
        GAMEBOY_COLOR;

        public static Model fromValue(int value) {
            return value == 0x80 ? GAMEBOY_COLOR : GAMEBOY;
        }
    }

    private enum Region {
        JAPAN,
        INTERNATIONAL;

        public static Region fromValue(int value) {
            return value == 0 ? JAPAN : INTERNATIONAL;
        }
    }

    private enum ROMSize {
        _32KB(32 * 1024),
        _64KB(64 * 1024),
        _128KB(128 * 1024),
        _256KB(256 * 1024),
        _512KB(512 * 1024),
        _1MB(1024 * 1024),
        _2MB(2048 * 1024);

        private final int expectedSize;

        ROMSize(int expectedSize) {
            this.expectedSize = expectedSize;
        }

        public String toString() {
            return super.toString().substring(1);
        }
    }

    private enum RAMSize {
        _2KB(2 * 1024, 1),
        _8KB(8 * 1024, 1),
        _32KB(32 * 1024, 4),
        _128KB(128 * 1024, 16);

        private final int expectedSize;
        private final int banks;

        RAMSize(int expectedSize, int banks) {
            this.expectedSize = expectedSize;
            this.banks = banks;
        }

        public String toString() {
            return super.toString().substring(1);
        }
    }

    private enum ROMType {
        ROM_ONLY(                       0x00),
        ROM_MBC1(                       0x01),
        ROM_MBC1_RAM(                   0x02),
        ROM_MBC1_RAM_BATTERY(           0x03),
        ROM_MBC2(                       0x05),
        ROM_MBC2_BATTERY(               0x06),
        ROM_RAM(                        0x08),
        ROM_RAM_BATTERY(                0x09),
        ROM_MMM01(                      0x0B),
        ROM_MMM01_SRAM(                 0x0C),
        ROM_MMM0_SRAM_BATTERY(          0x0D),
        ROM_MBC3_TIMER_BATTERY(         0x0F),
        ROM_MBC3_TIMER_RAM_BATTERY(     0x10),
        ROM_MBC3(                       0x11),
        ROM_MBC3_RAM(                   0x12),
        ROM_MBC3_RAM_BATTERY(           0x13),
        ROM_MBC5(                       0x19),
        ROM_MBC5_RAM(                   0x1A),
        ROM_MBC5_RAM_BATTERY(           0x1B),
        ROM_MBC5_RUMBLE(                0x1C),
        ROM_MBC5_RUMBLE_SRAM(           0x1D),
        ROM_MBC5_RUMBLE_SRAM_BATTERY(   0x1E),
        POCKET_CAMERA(                  0x1F),
        BANDAI_TAMA5(                   0xFD),
        HUDSON_HUC3(                    0xFE),
        HUDSOM_HUC1(                    0xFF);

        private final int value;

        ROMType(int value) {
            this.value = value;
        }

        public static ROMType fromValue(int value) {
            for (ROMType v : values()) {
                if (v.value == value) {
                    return v;
                }
            }
            throw new IllegalArgumentException("No " + ROMType.class.getSimpleName() + " with value " + DebugPrinter.hex(value, 2));
        }
    }
}
