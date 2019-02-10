package se.omfilm.gameboy.debug;

public interface Breakpoint {
    boolean matches(EmulatorState state);

    String displayText();
}
