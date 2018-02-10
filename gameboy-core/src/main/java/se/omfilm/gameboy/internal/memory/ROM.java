package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.util.DebugPrinter;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.function.BiFunction;

public class ROM {
    private static final Logger log = LoggerFactory.getLogger(ROM.class);

    private final byte[] data;
    private final ROMType romType;
    private final ROMSize romSize;
    private final RAMSize ramSize;
    private final String name;
    private final Model model;
    private final Region region;

    private Path ramPath;

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
            log.warn("ROM uses Super GameBoy functions, this is not implemented");
        }

        if (rom.romSize.expectedSize != data.length) {
            throw new IllegalArgumentException("Roms actual size doesn't match the expected " + rom.romSize.expectedSize + "!=" + data.length);
        }
        return rom;
    }

    public ROM saveRAM(Path path) {
        if (romType.battery) {
            ramPath = path;
        }
        return this;
    }

    private BankableRAM createRAMBanks() {
        if (ramPath != null) {
            try {
                return BankableRAM.toFile(ramSize.banks, ramPath.toFile());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return BankableRAM.inMemory(ramSize.banks);
    }

    public Memory createROMBanks() {
        BankableRAM ramBanks = createRAMBanks();
        ByteArrayMemory rom = new ByteArrayMemory(data);
        try {
            return romType.create(rom, ramBanks);
        } catch (Exception e) {
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

    public String name() {
        return name;
    }

    @SuppressWarnings("unused")
    private static Memory readOnly(ByteArrayMemory rom, BankableRAM ramBanks) {
        return new ReadOnlyMemory(rom);
    }

    @SuppressWarnings("unused")
    private static Memory unsupported(ByteArrayMemory rom, BankableRAM ramBanks) {
        throw new UnsupportedOperationException("Unsupported memory type");
    }

    private static String readGameName(byte[] rom) {
        byte[] name = new byte[16];
        System.arraycopy(rom, 0x0134, name, 0, 16);
        return new String(name).trim();
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
        ROM_ONLY(                       0x00, false, ROM::readOnly),
        ROM_MBC1(                       0x01, false, MBC1::new),
        ROM_MBC1_RAM(                   0x02, false, MBC1::new),
        ROM_MBC1_RAM_BATTERY(           0x03, true, MBC1::new),
        ROM_MBC2(                       0x05, false, MBC2::new),
        ROM_MBC2_BATTERY(               0x06, true, MBC2::new),
        ROM_RAM(                        0x08, false, ROM::unsupported),
        ROM_RAM_BATTERY(                0x09, true, ROM::unsupported),
        ROM_MMM01(                      0x0B, false, ROM::unsupported),
        ROM_MMM01_SRAM(                 0x0C, false, ROM::unsupported),
        ROM_MMM0_SRAM_BATTERY(          0x0D, true, ROM::unsupported),
        ROM_MBC3_TIMER_BATTERY(         0x0F, true, MBC3::new),
        ROM_MBC3_TIMER_RAM_BATTERY(     0x10, true, MBC3::new),
        ROM_MBC3(                       0x11, false, MBC3::new),
        ROM_MBC3_RAM(                   0x12, false, MBC3::new),
        ROM_MBC3_RAM_BATTERY(           0x13, true, MBC3::new),
        ROM_MBC5(                       0x19, false, MBC5::new),
        ROM_MBC5_RAM(                   0x1A, false, MBC5::new),
        ROM_MBC5_RAM_BATTERY(           0x1B, true, MBC5::new),
        ROM_MBC5_RUMBLE(                0x1C, false, MBC5::new),
        ROM_MBC5_RUMBLE_SRAM(           0x1D, false, MBC5::new),
        ROM_MBC5_RUMBLE_SRAM_BATTERY(   0x1E, true, MBC5::new),
        POCKET_CAMERA(                  0x1F, false, ROM::unsupported),
        BANDAI_TAMA5(                   0xFD, false, ROM::unsupported),
        HUDSON_HUC3(                    0xFE, false, ROM::unsupported),
        HUDSOM_HUC1(                    0xFF, false, ROM::unsupported);

        private final int value;
        private final boolean battery;
        private final BiFunction<ByteArrayMemory, BankableRAM, Memory> creator;

        ROMType(int value, boolean battery, BiFunction<ByteArrayMemory, BankableRAM, Memory> creator) {
            this.value = value;
            this.battery = battery;
            this.creator = creator;
        }

        public Memory create(ByteArrayMemory rom, BankableRAM ramBanks) {
            return creator.apply(rom, ramBanks);
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
