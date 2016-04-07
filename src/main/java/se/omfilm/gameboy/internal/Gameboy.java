package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.MMU;
import se.omfilm.gameboy.io.serial.ConsoleSerialConnection;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.util.Runner;

import java.io.IOException;

public class Gameboy {
    private final MMU memory;
    private final CPU cpu;
    private final GPU gpu;
    private final Timer timer;

    public Gameboy(Screen screen, byte[] bootData, byte[] romData) throws IOException {
        this.cpu = new CPU();
        this.gpu = new GPU(screen, this.cpu.interrupts);
        this.timer = new Timer(this.cpu.interrupts);
        this.memory = new MMU(bootData, romData, this.gpu, this.cpu.interrupts, timer, new ConsoleSerialConnection());
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
        timer.step(cycles);
        gpu.step(cycles);
        return cycles;
    }
}
