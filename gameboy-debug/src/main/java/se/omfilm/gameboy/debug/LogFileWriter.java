package se.omfilm.gameboy.debug;

import se.omfilm.gameboy.util.DebugPrinter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class LogFileWriter implements Consumer<EmulatorState> {
    private final PrintWriter out;

    public LogFileWriter(File logFile) throws FileNotFoundException {
        out = new PrintWriter(new FileOutputStream(logFile, false));
    }

    public void accept(EmulatorState emulatorState) {
        out.println(DebugPrinter.hex(emulatorState.programCounter().read(), 4) + "," + emulatorState.instructionType().name());
    }
}
