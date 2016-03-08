package se.omfilm.gameboy.io.screen;

import java.awt.*;

public interface Screen {
    int HEIGHT = 144;
    int WIDTH = 160;
    int FREQUENCY = 60;

    void turnOn();

    void turnOff();

    void setPixel(int x, int y, Color color);

    void draw();

    boolean isOn();
}
