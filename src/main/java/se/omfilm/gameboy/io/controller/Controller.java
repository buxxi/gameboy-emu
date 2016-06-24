package se.omfilm.gameboy.io.controller;

/**
 * This interface is called when the emulator needs to know which buttons is pressed.
 * Multiple controllers can be supported by making a composite-implementation of this that takes a list of controllers
 * and checks if a button has been pressed on any of the controllers.
 */
public interface Controller {
    boolean isPressed(Button button);

    enum Button {
        START,
        SELECT,
        A,
        B,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
