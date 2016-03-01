package se.omfilm.gameboy;

import java.awt.*;

public interface Screen {
    void initialize(int width, int height);

    void setPixel(int x, int y, Color color);

    void draw();
}
