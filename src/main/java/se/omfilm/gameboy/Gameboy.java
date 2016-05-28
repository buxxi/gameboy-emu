package se.omfilm.gameboy;

import se.omfilm.gameboy.internal.CPU;
import se.omfilm.gameboy.internal.GPU;
import se.omfilm.gameboy.internal.Interrupts;
import se.omfilm.gameboy.internal.Timer;
import se.omfilm.gameboy.internal.memory.MMU;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.ColorPalette;
import se.omfilm.gameboy.io.controller.Controller;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.io.serial.SerialConnection;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.util.Runner;

import java.io.IOException;

public class Gameboy {
    private final MMU memory;
    private final CPU cpu;
    private final GPU gpu;
    private final Timer timer;

    private final int frequency;
    private boolean running = false;

    public Gameboy(Screen screen, ColorPalette colorPalette, Controller controller, SerialConnection serial, ROM rom, boolean debug) throws IOException {
        this(screen, colorPalette, controller, serial, rom, Screen.FREQUENCY, debug);
    }

    public Gameboy(Screen screen, ColorPalette colorPalette, Controller controller, SerialConnection serial, ROM rom, int frequency, boolean debug) throws IOException {
        this.cpu = new CPU(debug);
        Interrupts interrupts = this.cpu.interrupts();
        this.gpu = new GPU(screen, colorPalette, interrupts);
        this.timer = new Timer(interrupts);
        this.memory = new MMU(rom, gpu, interrupts, timer, serial, controller);
        this.frequency = frequency;
    }

    public Gameboy withBootData(byte[] boot) {
        memory.withBootData(boot);
        return this;
    }

    public void run() throws InterruptedException {
        running = true;
        try {
            Runner.atFrequency(() -> {
                Runner.times(this::step, CPU.FREQUENCY / Screen.FREQUENCY);
                return running;
            }, frequency);
        } catch (Exception e) {
            DebugPrinter.debugException(e);
        }
    }

    public void stop() {
        running = false;
    }

    private Integer step() {
        int cycles = cpu.step(memory);
        memory.step(cycles);
        timer.step(cycles);
        gpu.step(cycles);
        cycles += cpu.interrupts().step(memory);
        return cycles;
    }
}
