package se.omfilm.gameboy.debug;

import se.omfilm.gameboy.Gameboy;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.ColorPalette;
import se.omfilm.gameboy.io.controller.Controller;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.io.serial.SerialConnection;
import se.omfilm.gameboy.io.sound.SoundPlayback;

import java.io.OutputStream;
import java.io.PrintStream;

public class DebuggableGameboy extends Gameboy {
    private final Debugger debugger = new Debugger();
    private final DebugGUI debugGUI = new DebugGUI(debugger);

    public DebuggableGameboy(Screen screen, ColorPalette colorPalette, Controller controller, SerialConnection serial, SoundPlayback soundPlayback, ROM rom, Speed speed) {
        super(screen, colorPalette, controller, serial, soundPlayback, rom, speed);
    }

    @Override
    public void run() {
        System.setErr(new PrintStream(OutputStream.nullOutputStream())); //Running in debugging mode we don't want to print the logs and this seems to be the easiest way to do that
        debugGUI.start();
        super.run();
    }

    @Override
    protected Integer step() {
        EmulatorState state = new EmulatorState(cpu, mmu);
        debugger.update(state);
        Integer result = super.step();
        debugger.checkBreakpoints();
        debugGUI.update();
        return result;
    }

    @Override
    protected Memory memory() {
        return debugger.getCurrentState().recordMemory(mmu);
    }
}
