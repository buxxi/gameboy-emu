package se.omfilm.gameboy.io.screen;

import java.awt.*;

public class NullScreen implements Screen {
    private boolean turnedOn = false;

    public void turnOn() {
        turnedOn = true;
    }

    public void turnOff() {
        turnedOn = false;
    }

    public void setPixel(int x, int y, Color color) {

    }

    public void draw() {

    }

    public boolean isOn() {
        return turnedOn;
    }
}
