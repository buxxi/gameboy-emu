package se.omfilm.gameboy;

import se.omfilm.gameboy.io.GPU;
import se.omfilm.gameboy.io.IOController;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.util.Timer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Gameboy {
    private final MMU memory;
    private final CPU cpu;
    private final GPU gpu;

    public Gameboy(Path bootPath, Path romPath, Screen screen) throws IOException {
        this.gpu = new GPU(screen);
        IOController ioController = new IOController(this.gpu);
        memory = new MMU(new ByteArrayMemory(Files.readAllBytes(bootPath)), new ROM(Files.readAllBytes(romPath)), ioController, this.gpu);
        this.cpu = new CPU();
    }

    public void run() throws InterruptedException {
        try {
            Timer.runForever(() -> {
                Timer.runTimes(this::step, CPU.FREQUENCY / Screen.FREQUENCY);
                return null;
            }, Screen.FREQUENCY);
        } catch (Exception e) {
            DebugPrinter.debugException(e);
        }
    }

    private Integer step() {
        int cycles = cpu.step(memory);
        gpu.step(cycles);
        return cycles;
    }
}
