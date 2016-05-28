package se.omfilm.gameboy.lwjgl;

import se.omfilm.gameboy.Gameboy;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.controller.GLFWCompositeController;
import se.omfilm.gameboy.io.screen.GLFWScreen;
import se.omfilm.gameboy.io.serial.ConsoleSerialConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Path bootPath = Paths.get(args[0]);
        Path romPath = Paths.get(args[1]);

        ROM rom = ROM.load(Files.readAllBytes(romPath));
        rom.print();

        GLFWCompositeController controller = new GLFWCompositeController();
        GLFWScreen screen = new GLFWScreen(rom.name(), controller);

        new Gameboy(screen, controller, new ConsoleSerialConnection(), rom, false).withBootData(Files.readAllBytes(bootPath)).run();
    }
}
