package se.omfilm.gameboy.io.color;

import se.omfilm.gameboy.internal.GPU.Shade;

import java.awt.*;

public interface ColorPalette {
    Color background(Shade shade);

    Color sprite(Shade shade, int index);
}
