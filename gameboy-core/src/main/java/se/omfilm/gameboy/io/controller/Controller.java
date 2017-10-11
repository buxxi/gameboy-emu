package se.omfilm.gameboy.io.controller;

/**
 * This interface is called when the emulator needs to know which buttons is pressed.
 * Multiple controllers can be supported by making a composite-implementation of this that takes a list of controllers
 * and checks if a button has been pressed on any of the controllers.
 *
 * isPressed should only need to update its value when the update()-method is called.
 */
public interface Controller {
    boolean isPressed(Button button);

    void update();

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
