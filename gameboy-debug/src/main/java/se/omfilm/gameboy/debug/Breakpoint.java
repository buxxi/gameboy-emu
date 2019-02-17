package se.omfilm.gameboy.debug;

public interface Breakpoint {
    boolean matches(EmulatorState currentState);

    String displayText();
}
