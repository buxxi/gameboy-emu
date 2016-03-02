package se.omfilm.gameboy.io;

import se.omfilm.gameboy.Screen;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TempFileScreen implements Screen {
    private BufferedImage buffer;
    private ImageWriter writer;
    private ImageOutputStream imageStream;

    public void turnOn() {
        try {
            writer = ImageIO.getImageWritersByFormatName("gif").next();
            imageStream = ImageIO.createImageOutputStream(new FileOutputStream(File.createTempFile("gameboy", ".gif")));
            writer.setOutput(imageStream);
            writer.prepareWriteSequence(null);
            newFrame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPixel(int x, int y, Color color) {
        buffer.setRGB(x, y, color.getRGB());
    }

    public void turnOff() {
        try {
            writer.endWriteSequence();
            imageStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw() {
        try {
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(buffer), iwp);

            IIOImage ii = new IIOImage(buffer, null, metadata);
            writer.writeToSequence(ii, null);
            //TODO: figure out to increase frame rate or drop frames to an acceptable speed
            newFrame();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void newFrame() {
        buffer = new BufferedImage(Screen.WIDTH, Screen.HEIGHT, BufferedImage.TYPE_INT_RGB);
    }
}
