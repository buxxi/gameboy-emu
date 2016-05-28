package se.omfilm.gameboy.lwjgl;

import se.omfilm.gameboy.Gameboy;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.ColorPalette;
import se.omfilm.gameboy.io.color.FixedColorPalette;
import se.omfilm.gameboy.io.controller.GLFWCompositeController;
import se.omfilm.gameboy.io.screen.GLFWScreen;
import se.omfilm.gameboy.io.serial.ConsoleSerialConnection;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Path bootPath = Paths.get(args[0]);
        Path romPath = Paths.get(args[1]);

        ROM rom = ROM.load(Files.readAllBytes(romPath));
        rom.print();

        GLFWCompositeController controller = new GLFWCompositeController();
        GLFWScreen screen = new GLFWScreen(rom.name(), controller);
        ColorPalette palette = (ColorPalette) FixedColorPalette.class.getMethod(args[2]).invoke(null);

        new Gameboy(screen, palette, controller, new ConsoleSerialConnection(), rom, false).withBootData(Files.readAllBytes(bootPath)).run();
    }
}
