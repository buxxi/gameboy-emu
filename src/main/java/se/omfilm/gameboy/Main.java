package se.omfilm.gameboy;

import se.omfilm.gameboy.internal.Gameboy;
import se.omfilm.gameboy.io.screen.Screen;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        //TODO: make nicer command line arguments
        new Gameboy((Screen) Class.forName(args[2]).newInstance(), Files.readAllBytes(Paths.get(args[0])), Files.readAllBytes(Paths.get(args[1]))).run();
    }
}
