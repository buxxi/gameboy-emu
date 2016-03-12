package se.omfilm.gameboy;

import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.util.Runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Gameboy {
    private final MMU memory;
    private final CPU cpu;
    private final GPU gpu;

    public Gameboy(Path bootPath, Path romPath, Screen screen) throws IOException {
        this.cpu = new CPU();
        this.gpu = new GPU(screen, this.cpu.interrupts);
        memory = new MMU(new ByteArrayMemory(Files.readAllBytes(bootPath)), new ROM(Files.readAllBytes(romPath)), this.gpu, this.cpu.interrupts, new Timer(this.cpu.flags));
    }

    public void run() throws InterruptedException {
        try {
            Runner.atFrequence(() -> {
                Runner.times(this::step, CPU.FREQUENCY / Screen.FREQUENCY);
                return null;
            }, Screen.FREQUENCY);
        } catch (Exception e) {
            DebugPrinter.debugException(e);
        }
    }

    private Integer step() {
        int cycles = cpu.step(memory);
        gpu.step(cycles);
        cpu.interruptStep(memory);
        return cycles;
    }
}
