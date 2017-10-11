package se.omfilm.gameboy.io.controller;

public class NullController implements Controller {
    public boolean isPressed(Button button) {
        return false;
    }

    public void update() {

    }
}
