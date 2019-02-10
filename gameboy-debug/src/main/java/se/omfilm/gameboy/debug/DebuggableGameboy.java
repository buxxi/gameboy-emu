package se.omfilm.gameboy.debug;

import se.omfilm.gameboy.Gameboy;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.ColorPalette;
import se.omfilm.gameboy.io.controller.Controller;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.io.serial.SerialConnection;
import se.omfilm.gameboy.io.sound.SoundPlayback;

import java.util.LinkedList;
import java.util.Queue;

public class DebuggableGameboy extends Gameboy {
    private final Debugger debugger = new Debugger();
    private final DebugGUI debugGUI = new DebugGUI(debugger);

    public DebuggableGameboy(Screen screen, ColorPalette colorPalette, Controller controller, SerialConnection serial, SoundPlayback soundPlayback, ROM rom, Speed speed) {
        super(screen, colorPalette, controller, serial, soundPlayback, rom, speed);
    }

    @Override
    public void run() {
        debugGUI.start();
        super.run();
    }

    @Override
    protected Integer step() {
        EmulatorState state = new EmulatorState(cpu, memory);
        debugger.update(state);
        debugGUI.update();
        return super.step();
    }
}
