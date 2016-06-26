package se.omfilm.gameboy;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.ColorPalette;
import se.omfilm.gameboy.io.controller.Controller;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.io.serial.SerialConnection;
import se.omfilm.gameboy.io.sound.SoundPlayback;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.util.Runner;

import java.io.IOException;

public class Gameboy {
    private final MMU memory;
    private final CPU cpu;
    private final PPU ppu;
    private final APU apu;
    private final Timer timer;
    private final Input input;

    private final int frequency;
    private boolean running = false;

    public Gameboy(Screen screen, ColorPalette colorPalette, Controller controller, SerialConnection serial, SoundPlayback soundPlayback, ROM rom, Speed speed, boolean debug) throws IOException {
        this.cpu = new CPU(debug);
        this.ppu = new PPU(screen, colorPalette);
        this.apu = new APU(soundPlayback);
        this.timer = new Timer();
        input = new Input(controller);
        this.memory = new MMU(rom, ppu, apu, cpu.interrupts(), timer, serial, input);
        this.frequency = speed.frequency;
    }

    public Gameboy withBootData(byte[] boot) {
        memory.withBootData(boot);
        return this;
    }

    public void run() throws InterruptedException {
        running = true;
        try {
            Runner.atFrequency(this::stepFrequency, frequency);
        } catch (Exception e) {
            DebugPrinter.debugException(e);
        }
    }

    public void reset() {
        cpu.reset();
        apu.reset();
        ppu.reset();
        timer.reset();
    }

    public void stop() {
        running = false;
    }

    private boolean stepFrequency() throws Exception {
        Runner.times(this::step, CPU.FREQUENCY / Screen.FREQUENCY);
        return running;
    }

    private Integer step() {
        Interrupts interrupts = cpu.interrupts();
        int cycles = cpu.step(memory);
        input.step(cycles, interrupts);
        timer.step(cycles, interrupts);
        ppu.step(cycles, interrupts);
        apu.step(cycles, interrupts);
        cycles += interrupts.step(memory);
        return cycles;
    }

    public enum Speed {
        NORMAL(Screen.FREQUENCY),
        DOUBLE(Screen.FREQUENCY * 2),
        UNLIMITED(Integer.MAX_VALUE);

        private final int frequency;

        Speed(int frequency) {
            this.frequency = frequency;
        }
    }
}
