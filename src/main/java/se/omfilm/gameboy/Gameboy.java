package se.omfilm.gameboy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Gameboy {
    private final Memory memory;
    private final CPU cpu;

    public Gameboy(Path bootPath, Path romPath) throws IOException {
        this(new Memory(Files.readAllBytes(bootPath), verifyRom(Files.readAllBytes(romPath))), new CPU());
    }

    public Gameboy(Memory memory, CPU cpu) {
        this.memory = memory;
        this.cpu = cpu;
    }

    public void run() {
        try {
            while (true) {
                cpu.runNext(memory);
            }
        } catch (IllegalArgumentException e) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.err.println(e);
            System.err.println(InstructionType.values().length + " instructions implemented of 512");
        }
    }

    private static byte[] verifyRom(byte[] rom) {
        System.out.println("Game:\t\t" + readGameName(rom));
        if (rom[0x146] != 0) {
            throw new IllegalArgumentException("Can only handle the original GameBoy");
        }
        if (rom[0x147] != 0) {
            throw new IllegalArgumentException("Can only handle Cartridge Types of ROM Only");
        }
        ROM_SIZE rom_size = ROM_SIZE.values()[rom[0x148]];
        if (rom_size.expectedSize != rom.length) {
            throw new IllegalArgumentException("Roms actual size doesn't match the expected " + rom_size.expectedSize + "!=" + rom.length);
        }
        System.out.println("ROM Size:\t" + rom_size);
        if (rom[0x149] != 0) {
            throw new IllegalArgumentException("Can only handle RAM Size None");
        }

        System.out.println("Region:\t\t" + (rom[0x14A] == 0 ? "Japan" : "International"));
        System.out.println("C-check:\t" + (rom[0x14D]));
        System.out.println("Checksum:\t" + DebugPrinter.hex((rom[0x14E] << 8) + rom[0x14F], 4));
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
}
