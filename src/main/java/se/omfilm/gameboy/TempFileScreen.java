package se.omfilm.gameboy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TempFileScreen implements Screen {
    private boolean written = false;
    private BufferedImage result;

    public void initialize(int width, int height) {
        result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void setPixel(int x, int y, Color color) {
        result.setRGB(x, y, color.getRGB());
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
