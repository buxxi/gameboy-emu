package se.omfilm.gameboy;

import java.awt.*;

public interface Screen {
    int HEIGHT = 144;
    int WIDTH = 160;
    int FREQUENCY = 60;

    void initialize();

    void setPixel(int x, int y, Color color);

    void draw();
}
