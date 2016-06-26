package se.omfilm.gameboy.lwjgl;

import se.omfilm.gameboy.Gameboy;
import se.omfilm.gameboy.internal.PPU.Shade;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.color.ColorPalette;
import se.omfilm.gameboy.io.color.FixedColorPalette;
import se.omfilm.gameboy.io.color.MultiColorPalette;
import se.omfilm.gameboy.io.controller.GLFWCompositeController;
import se.omfilm.gameboy.io.screen.GLFWScreen;
import se.omfilm.gameboy.io.serial.ConsoleSerialConnection;
import se.omfilm.gameboy.io.sound.NullSoundPlayback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Path bootPath = Paths.get(args[0]);
        Path romPath = Paths.get(args[1]);
        Path savePath = romPath.resolveSibling(romPath.getFileName().toString() + ".sav");

        ColorPalette palette = parsePalette(args[2]);
        GLFWScreen.Mode mode = GLFWScreen.Mode.valueOf(args[3]);
        Gameboy.Speed speed = Gameboy.Speed.valueOf(args[4]);

        ROM rom = ROM.load(Files.readAllBytes(romPath)).saveRAM(savePath);
        rom.print();

        GLFWCompositeController controller = new GLFWCompositeController();
        GLFWScreen screen = new GLFWScreen(rom.name(), controller, mode);

        new Gameboy(screen, palette, controller, new ConsoleSerialConnection(), new NullSoundPlayback(), rom, speed, false).withBootData(Files.readAllBytes(bootPath)).run();
    }

    private static ColorPalette parsePalette(String arg) {
        String[] parts = arg.split(",");
        if (parts.length == 1) {
            if (arg.equals("ALL_RANDOM")) {
                return new FixedColorPalette(
                        randomPalette().background(Shade.DARKEST),
                        randomPalette().background(Shade.DARK),
                        randomPalette().background(Shade.LIGHT),
                        randomPalette().background(Shade.LIGHTEST)
                );
            } else if (arg.equals("RANDOM")) {
                return randomPalette();
            }
            return FixedColorPalette.PRESET.valueOf(arg).getPalette();
        } else if (parts.length == 4) {
            return new MultiColorPalette(parsePalette(parts[0]), parsePalette(parts[1]), parsePalette(parts[2]), parsePalette(parts[3]));
        } else {
            throw new IllegalArgumentException("Can't handle " + arg + " as palette");
        }
    }

    private static ColorPalette randomPalette() {
        FixedColorPalette.PRESET[] values = FixedColorPalette.PRESET.values();
        return values[new Random().nextInt(values.length)].getPalette();
    }
}
