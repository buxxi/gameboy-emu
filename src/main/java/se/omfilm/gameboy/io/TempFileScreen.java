package se.omfilm.gameboy.io;

import se.omfilm.gameboy.Screen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TempFileScreen implements Screen {
    private boolean written = false;
    private BufferedImage result;

    public void initialize() {
        result = new BufferedImage(Screen.WIDTH, Screen.HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    public void setPixel(int x, int y, Color color) {
        try {
            result.setRGB(x, y, color.getRGB());
        } catch (Exception e) {} //TODO: should never call this while index out of bounds
    }

    public void draw() {
        if (!written) {
            written = true;
            try {
                ImageIO.write(result, "png", File.createTempFile("gameboy", ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
