package se.omfilm.gameboy;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        new Gameboy(Paths.get(args[0]), Paths.get(args[1])).run();
    }
}
