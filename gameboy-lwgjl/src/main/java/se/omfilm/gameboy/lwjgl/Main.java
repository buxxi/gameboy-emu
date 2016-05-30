package se.omfilm.gameboy.lwjgl;

import se.omfilm.gameboy.Gameboy;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.ColorPalette;
import se.omfilm.gameboy.io.color.FixedColorPalette;
import se.omfilm.gameboy.io.color.MultiColorPalette;
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
        ColorPalette palette = parsePalette(args[2]);

        new Gameboy(screen, palette, controller, new ConsoleSerialConnection(), rom, false).withBootData(Files.readAllBytes(bootPath)).run();
    }

    private static ColorPalette parsePalette(String arg) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String[] parts = arg.split(",");
        if (parts.length == 1) {
            return (ColorPalette) FixedColorPalette.class.getMethod(parts[0]).invoke(null);
        } else if (parts.length == 3) {
            return new MultiColorPalette(parsePalette(parts[0]), parsePalette(parts[1]), parsePalette(parts[2]));
        } else {
            throw new IllegalArgumentException("Can't handle " + arg + " as palette");
        }
    }
}
