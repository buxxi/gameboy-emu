package se.omfilm.gameboy;

import se.omfilm.gameboy.io.screen.Screen;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        //TODO: make nicer command line arguments
        new Gameboy(Paths.get(args[0]), Paths.get(args[1]), (Screen) Class.forName(args[2]).newInstance()).run();
    }
}
