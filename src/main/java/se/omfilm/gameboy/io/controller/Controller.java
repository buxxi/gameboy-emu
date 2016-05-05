package se.omfilm.gameboy.io.controller;

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
