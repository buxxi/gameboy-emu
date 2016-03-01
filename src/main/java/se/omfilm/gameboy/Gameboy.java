package se.omfilm.gameboy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Gameboy {
    private final MMU memory;
    private final CPU cpu;
    private final GPU gpu;
    private final Screen screen = new ConsoleScreen();

    public Gameboy(Path bootPath, Path romPath) throws IOException {
        this.gpu = new GPU(new ByteArrayMemory(Memory.MemoryType.VIDEO_RAM.allocate()));
        IOController ioController = new IOController(this.gpu);
        memory = new MMU(new ByteArrayMemory(Files.readAllBytes(bootPath)), verifyRom(Files.readAllBytes(romPath)), ioController, this.gpu);
        this.cpu = new CPU();
    }

    public void run() {
        try {
            while (true) {
                int cycles = cpu.step(memory);
                gpu.step(cycles, screen);
            }
        } catch (IllegalArgumentException e) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.err.println(e);
            System.err.println(Instruction.InstructionType.values().length + " instructions implemented of 512");
        }
    }

    private static Memory verifyRom(byte[] rom) {
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
        return new ByteArrayMemory(rom);
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
