package se.omfilm.gameboy;

import se.omfilm.gameboy.internal.Gameboy;
import se.omfilm.gameboy.io.controller.Controller;
import se.omfilm.gameboy.io.controller.SwingController;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.io.screen.SwingScreen;
import se.omfilm.gameboy.io.serial.ConsoleSerialConnection;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        //TODO: make nicer command line arguments
        Screen screen = (Screen) Class.forName(args[2]).newInstance();
        Controller controller = (Controller) Class.forName(args[3]).newInstance();
        //TODO: handle this in some other way which doesn't need to check the instance of every type
        if (controller instanceof SwingController) {
            if (screen instanceof SwingScreen) {
                ((SwingScreen) screen).addKeyListener((SwingController) controller);
            }
        }

        byte[] bootData = Files.readAllBytes(Paths.get(args[0]));
        byte[] romData = Files.readAllBytes(Paths.get(args[1]));
        new Gameboy(screen, controller, new ConsoleSerialConnection(), bootData, romData, true).run();
    }
}
