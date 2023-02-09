package se.omfilm.gameboy.io.screen;

import se.omfilm.gameboy.io.color.Color;

/**
 * The GPU outputs its finished pixelbuffer to this interface.
 * setPixel(...) will be called for every row with every x-value in that order,
 * after that draw() is called, so nothing should be shown for the user before draw() has been called.
 * <p>
 * turnOn() and turnOff() can be called during runtime multiple times in a row,
 * so the underlying implementation should handle state for that.
 * <p>
 * The screens width, height and frequency is provided here but that doesn't mean it can't be scaled or run faster.
 */
public interface Screen {
    int HEIGHT = 144;
    int WIDTH = 160;
    int FREQUENCY = 60;

    void turnOn();

    void turnOff();

    void setPixel(int x, int y, Color color);

    void draw();
}
